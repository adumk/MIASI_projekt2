package com.rental.system;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("ReservationFlowSystemTest — end-to-end reservation lifecycle")
class ReservationFlowSystemTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test_rental_system")
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

    @BeforeEach
    void stubFleetAvailable() {
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":"vehicle-001","status":"AVAILABLE","category":"STANDARD"}
                                """)));

        wireMock.stubFor(get(urlPathMatching("/api/v1/customers/.*/can-rent"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")));

        wireMock.stubFor(get(urlPathMatching("/api/v1/quotes.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"category":"STANDARD","rentalDays":7,
                                 "dailyRateMinorUnits":15000,"totalMinorUnits":45000,"currency":"PLN"}
                                """)));
    }

    @Test
    @DisplayName("Should complete full reservation flow: create → confirm → activate")
    void completeReservationFlowFromCreateToActive() {
        // Step 1: Create reservation
        Map<String, Object> reservationRequest = Map.of(
                "customerId", "system-customer-001",
                "vehicleId", "vehicle-001",
                "email", "test@example.com",
                "startDate", LocalDate.now().plusDays(1).toString(),
                "endDate", LocalDate.now().plusDays(8).toString());

        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
                "/api/v1/reservations", reservationRequest, Map.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String rentalId = (String) createResponse.getBody().get("rentalId");
        assertThat(rentalId).isNotBlank();

        // Step 2: Confirm payment
        ResponseEntity<Map> confirmResponse = restTemplate.postForEntity(
                "/api/v1/reservations/" + rentalId + "/confirm", null, Map.class);
        assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Step 3: Activate rental
        ResponseEntity<Map> activateResponse = restTemplate.postForEntity(
                "/api/v1/rentals/" + rentalId + "/activate", null, Map.class);
        assertThat(activateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activateResponse.getBody().get("status")).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should reject reservation when vehicle is unavailable")
    void reservationShouldBeRejectedWhenVehicleIsUnavailable() {
        // Override stub: vehicle RENTED
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":"vehicle-rented-001","status":"RENTED","category":"STANDARD"}
                                """)));

        Map<String, Object> reservationRequest = Map.of(
                "customerId", "system-customer-002",
                "vehicleId", "vehicle-rented-001",
                "email", "test2@example.com",
                "startDate", LocalDate.now().plusDays(1).toString(),
                "endDate", LocalDate.now().plusDays(8).toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/v1/reservations", reservationRequest, Map.class);

        assertThat(response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError()).isTrue();
    }
}