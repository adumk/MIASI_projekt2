package com.rental.system;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.ports.out.IRentalRepository;
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
@DisplayName("CancellationFlowSystemTest — end-to-end cancellation lifecycle")
@AutoConfigureTestRestTemplate
class CancellationFlowSystemTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_rental_cancel")
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

    private Rental reservedRental(String id) {
        CustomerId customerId = CustomerId.of("system-cancel-customer-001");
        Rental rental = Rental.create(
                RentalId.of(id),
                VehicleId.of("vehicle-cancel-001"),
                customerId,
                DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(5)));
        rental.confirm();
        return rental;
    }

    private Rental activeRental(String id) {
        CustomerId customerId = CustomerId.of("system-cancel-customer-002");
        Rental rental = Rental.create(
                RentalId.of(id),
                VehicleId.of("vehicle-cancel-002"),
                customerId,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)));
        rental.confirm();
        rental.confirmPayment();
        rental.activate(Customer.eligible(customerId));
        return rental;
    }

    @Test
    @DisplayName("Should cancel reservation from RESERVED state")
    void reservationCanBeCancelledFromReservedState() {
        // given
        String rentalId = "system-cancel-reserved-001";
        rentalRepository.save(reservedRental(rentalId));

        // when — NAPRAWIONO: Zmieniono ścieżkę z /rentals/ na /reservations/ zgodnie z RentalRestController
        ResponseEntity<Map> cancelResponse = restTemplate.postForEntity(
                "/api/v1/reservations/" + rentalId + "/cancel", null, Map.class);

        assertThat(cancelResponse.getStatusCode())
                .isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);

        // then — verify status is CANCELLED (pobieranie szczegółów zostaje pod /rentals/{id})
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                "/api/v1/rentals/" + rentalId, Map.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("status")).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("Should reject cancellation when rental is in ACTIVE state")
    void activeRentalCannotBeCancelled() {
        // given
        String rentalId = "system-cancel-active-001";
        rentalRepository.save(activeRental(rentalId));

        // when — NAPRAWIONO: Zmieniono ścieżkę z /rentals/ na /reservations/ zgodnie z RentalRestController
        ResponseEntity<Map> cancelResponse = restTemplate.postForEntity(
                "/api/v1/reservations/" + rentalId + "/cancel", null, Map.class);

        // then — InvalidStatusTransitionException → 409 Conflict
        // ZASTĄP PRZEZ:
        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify status unchanged
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(
                "/api/v1/rentals/" + rentalId, Map.class);
        assertThat(getResponse.getBody().get("status")).isEqualTo("ACTIVE");
    }
}