package com.rental.system;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Money;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.ports.out.IRentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("ReturnVehicleFlowSystemTest — end-to-end return lifecycle")
@AutoConfigureTestRestTemplate
class ReturnVehicleFlowSystemTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_rental_return")
            .withUsername("qa_user")
            .withPassword("secure_qa_pass");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("rental.fleet-service-url", () -> "http://localhost:" + wireMock.getPort());
        registry.add("rental.customer-service-url", () -> "http://localhost:" + wireMock.getPort());
        registry.add("rental.billing-service-url", () -> "http://localhost:" + wireMock.getPort());
        registry.add("rental.events.publisher", () -> "logging");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IRentalRepository rentalRepository;

    private static final String RENTAL_ID = "system-return-rental-001";
    private static final CustomerId CUSTOMER_ID = CustomerId.of("system-return-customer-001");
    private static final VehicleId VEHICLE_ID = VehicleId.of("vehicle-return-001");

    @BeforeEach
    void setUpActiveRentalAndStubs() {
        // Persist ACTIVE rental directly via repository
        Rental rental = Rental.create(
                RentalId.of(RENTAL_ID), VEHICLE_ID, CUSTOMER_ID,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(1)));
        rental.confirm();
        rental.confirmPayment();
        rental.activate(Customer.eligible(CUSTOMER_ID));
        rentalRepository.save(rental);

        // Stub fleet resolveCategory
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":"vehicle-return-001","status":"RENTED","category":"STANDARD"}
                                """)));

// Stub billing quote
        wireMock.stubFor(get(urlPathEqualTo("/api/v1/quotes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"category":"STANDARD","rentalDays":4,
                                 "dailyRateMinorUnits":15000,"totalMinorUnits":30000,"currency":"PLN"}
                                """)));
    }

    @Test
    @DisplayName("Should complete return flow: ACTIVE → COMPLETED")
    void completeReturnFlow() {
// Step 1: Return vehicle
        Map<String, Object> returnRequest = Map.of(
                "actualReturnDate", LocalDate.now().toString(),
                "mileage", 45000,
                "inspectionNotes", "No visible damage");

        ResponseEntity<Map> returnResponse = restTemplate.postForEntity(
                "/api/v1/rentals/" + RENTAL_ID + "/return",
                returnRequest,
                Map.class);

        assertThat(returnResponse.getStatusCode())
                .isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);

        // Step 2: Verify status is COMPLETED
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                "/api/v1/rentals/" + RENTAL_ID, Map.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("status")).isEqualTo("COMPLETED");
    }
}