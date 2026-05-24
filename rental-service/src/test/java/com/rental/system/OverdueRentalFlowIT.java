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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("System flow: overdue rental")
class OverdueRentalFlowIT {

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
    @DisplayName("should transition active rental to OVERDUE and persist that state across all layers via API")
    void shouldMarkActiveRentalAsOverdueAcrossAllLayers() throws Exception {
        // Arrange - setup prerequisites
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        vehicleRepository.save(Vehicle.create(vehicleId));

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(2).toString();

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Wywołanie API: Utworzenie rezerwacji
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        Rental created = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);

        // Krok pośredni: Potwierdzenie rezerwacji (jeśli odbywa się wewnętrznie w bazie danych)
        created.confirm();
        rentalRepository.save(created);

        // Wywołanie API: Wydanie pojazdu (Rent)
        // UWAGA: Dopasuj ścieżkę URL (np. /api/v1/rentals/{id}/rent lub /api/v1/reservations/{id}/rent) do swojego kontrolera
        mockMvc.perform(post("/api/v1/rentals/" + created.id().value() + "/rent"))
                .andExpect(status().is2xxSuccessful());

        // Act - Wywołanie API: Oznaczenie jako zaległe (Overdue)
        // UWAGA: Dopasuj ścieżkę URL do swojego kontrolera
        mockMvc.perform(post("/api/v1/rentals/" + created.id().value() + "/overdue"))
                .andExpect(status().is2xxSuccessful());

        // Assert - Weryfikacja stanu w bazie danych przez adapter wyjściowy
        Rental overdue = rentalRepository.findById(created.id());
        assertThat(overdue.status()).isEqualTo(RentalStatus.OVERDUE);
    }

    @Test
    @DisplayName("should not allow marking a non-active rental as overdue — state must be preserved")
    void shouldRejectMarkingNonActiveRentalAsOverdue() throws Exception {
        // Arrange
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());
        vehicleRepository.save(Vehicle.create(vehicleId));

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(2).toString();

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Wywołanie API: Utworzenie rezerwacji (w tym momencie status to RESERVED - czyli non-active)
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        Rental created = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);

        // Act & Assert - Próba oznaczenia nieaktywnego wypożyczenia jako zaległe przez API powinna zwrócić błąd klienta (np. 400 lub 422)
        mockMvc.perform(post("/api/v1/rentals/" + created.id().value() + "/overdue"))
                .andExpect(status().is4xxClientError());

        // Weryfikacja czy stan w bazie danych pozostał nienaruszony (nie zmienił się na OVERDUE)
        Rental reloaded = rentalRepository.findById(created.id());
        assertThat(reloaded.status()).isNotEqualTo(RentalStatus.OVERDUE);
    }
}