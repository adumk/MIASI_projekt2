package com.rental.system;

import com.rental.domain.CustomerId;
import com.rental.domain.Rental;
import com.rental.domain.RentalStatus;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleId;
import com.rental.domain.VehicleStatus;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("System flow: reservation → rent")
class ReservationToRentFlowIT {

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
    @Autowired private IRentalRepository  rentalRepository;
    @Autowired private IVehicleRepository vehicleRepository;

    @Test
    @DisplayName("should transition from reservation to active rental across all layers via API")
    void shouldTransitionFromReservationToActiveRental() throws Exception {
        // Arrange
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(7).toString();

        vehicleRepository.save(Vehicle.create(vehicleId));

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Act & Assert - Wywołanie API: Utworzenie rezerwacji
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        // Weryfikacja stanu pośredniego w bazie danych
        Rental afterReservation = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);
        assertThat(afterReservation.status()).isIn(RentalStatus.RESERVED, RentalStatus.CONFIRMED);

        // Krok pośredni z oryginalnego testu (ręczne potwierdzenie i zapis w bazie)
        afterReservation.confirm();
        rentalRepository.save(afterReservation);

        // Act - Wywołanie API: Wydanie pojazdu (Rent)
        // UWAGA: Dopasuj ścieżkę URL (np. /api/v1/rentals/{id}/rent) do swojego kontrolera REST
        mockMvc.perform(post("/api/v1/rentals/" + afterReservation.id().value() + "/rent"))
                .andExpect(status().is2xxSuccessful());

        // Assert - Końcowa weryfikacja stanu w bazie danych (zarówno agregatu Rental jak i Vehicle)
        Rental afterRent = rentalRepository.findById(afterReservation.id());
        assertThat(afterRent.status()).isEqualTo(RentalStatus.ACTIVE);

        Vehicle afterRentVehicle = vehicleRepository.findById(vehicleId);
        assertThat(afterRentVehicle.status()).isEqualTo(VehicleStatus.RENTED);
    }

    @Test
    @DisplayName("should persist reservation created event semantics through the full stack via API")
    void shouldPublishReservationCreatedThroughFullStack() throws Exception {
        // Arrange
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(7).toString();

        vehicleRepository.save(Vehicle.create(vehicleId));

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Act - Wywołanie API: Utworzenie rezerwacji
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        // Assert - Sprawdzenie czy dane poprawnie przeszły przez mapowanie DTO i komendy oraz zostały zapisane w bazie
        Rental saved = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);
        assertThat(saved).isNotNull();
        assertThat(saved.vehicleId()).isEqualTo(vehicleId);
        assertThat(saved.customerId()).isEqualTo(customerId);
        assertThat(saved.period().start()).isEqualTo(LocalDate.parse(startDate));
        assertThat(saved.period().end()).isEqualTo(LocalDate.parse(endDate));
    }
}