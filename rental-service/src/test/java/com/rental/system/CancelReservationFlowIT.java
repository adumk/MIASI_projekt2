package com.rental.system;

import com.rental.domain.CustomerId;
import com.rental.domain.Rental;
import com.rental.domain.RentalStatus;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleId;
import com.rental.ports.out.IRentalRepository;
import com.rental.ports.out.IVehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("System flow: cancel reservation")
class CancelReservationFlowIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rental_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private IRentalRepository rentalRepository;
    @Autowired private IVehicleRepository vehicleRepository;

    @Test
    @DisplayName("should transition rental to CANCELLED and persist that state across all layers via API")
    void shouldCancelReservationAndPersistCancelledState() throws Exception {
        // Arrange - setup system prerequisites
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        vehicleRepository.save(Vehicle.create(vehicleId));

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(7).toString();

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Act 1: Input Adapter -> POST /reservations
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        Rental created = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);
        assertThat(created.status()).isIn(RentalStatus.RESERVED, RentalStatus.CONFIRMED);

        // Act 2: Input Adapter -> Cancel reservation
        // UWAGA: Ścieżka URL zależy od implementacji w Twoim kontrolerze.
        // Jeśli używasz innej metody np. DELETE /api/v1/reservations/{id}, zmień 'post' na 'delete'
        mockMvc.perform(post("/api/v1/reservations/" + created.id().value() + "/cancel"))
                .andExpect(status().is2xxSuccessful());

        // Assert: Output Adapter -> Verify DB state
        Rental cancelled = rentalRepository.findById(created.id());
        assertThat(cancelled.status()).isEqualTo(RentalStatus.CANCELLED);
    }

    @Test
    @DisplayName("should persist CANCELLED state so that reloading the aggregate returns CANCELLED via API")
    void shouldPersistCancelledStateAcrossRepositoryRoundTrip() throws Exception {
        // Arrange
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        vehicleRepository.save(Vehicle.create(vehicleId));

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(7).toString();

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Act 1: Input Adapter -> POST /reservations
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        Rental created = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);

        // Act 2: Input Adapter -> Cancel reservation
        mockMvc.perform(post("/api/v1/reservations/" + created.id().value() + "/cancel"))
                .andExpect(status().is2xxSuccessful());

        // Assert: Output Adapter -> Verify state preservation
        Rental reloaded = rentalRepository.findById(created.id());
        assertThat(reloaded.domainEvents()).isEmpty();
        assertThat(reloaded.status()).isEqualTo(RentalStatus.CANCELLED);
    }
}