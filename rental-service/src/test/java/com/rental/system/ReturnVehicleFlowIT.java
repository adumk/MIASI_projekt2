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
@DisplayName("System flow: return vehicle")
class ReturnVehicleFlowIT {

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
    @DisplayName("should transition rental to COMPLETED and vehicle to AVAILABLE across all layers via API")
    void shouldCompleteRentalAndReturnVehicleToAvailable() throws Exception {
        // Arrange
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(7).toString();
        String actualReturnDate = LocalDate.now().plusDays(8).toString();

        vehicleRepository.save(Vehicle.create(vehicleId));

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Act 1: API Call - Create Reservation
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        Rental created = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);

        // Intermediate step: Confirm reservation (simulating internal or database-level confirmation)
        created.confirm();
        rentalRepository.save(created);

        // Act 2: API Call - Rent Vehicle
        // NOTE: Adjust the endpoint path if your REST Controller uses a different URI structure
        mockMvc.perform(post("/api/v1/rentals/" + created.id().value() + "/rent"))
                .andExpect(status().is2xxSuccessful());

        // Act 3: API Call - Return Vehicle
        String returnPayload = """
            {
              "actualReturnDate": "%s"
            }
            """.formatted(actualReturnDate);

        mockMvc.perform(post("/api/v1/rentals/" + created.id().value() + "/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnPayload))
                .andExpect(status().is2xxSuccessful());

        // Assert: Verify final state in Output Adapter (Database)
        Rental completed = rentalRepository.findById(created.id());
        assertThat(completed.status()).isEqualTo(RentalStatus.COMPLETED);

        Vehicle returned = vehicleRepository.findById(vehicleId);
        assertThat(returned.status()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("should persist final cost in completed rental after return via API")
    void shouldPersistFinalCostInCompletedRental() throws Exception {
        // Arrange
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        String startDate = LocalDate.now().plusDays(1).toString();
        String endDate   = LocalDate.now().plusDays(7).toString();
        String actualReturnDate = LocalDate.now().plusDays(8).toString();

        vehicleRepository.save(Vehicle.create(vehicleId));

        String createPayload = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(customerId.value(), vehicleId.value(), startDate, endDate);

        // Act 1: API Call - Create Reservation
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        Rental created = rentalRepository.findByVehicleIdAndCustomerId(vehicleId, customerId);

        // Intermediate step: Confirm reservation
        created.confirm();
        rentalRepository.save(created);

        // Act 2: API Call - Rent Vehicle
        mockMvc.perform(post("/api/v1/rentals/" + created.id().value() + "/rent"))
                .andExpect(status().is2xxSuccessful());

        // Act 3: API Call - Return Vehicle
        String returnPayload = """
            {
              "actualReturnDate": "%s"
            }
            """.formatted(actualReturnDate);

        mockMvc.perform(post("/api/v1/rentals/" + created.id().value() + "/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnPayload))
                .andExpect(status().is2xxSuccessful());

        // Assert: Verify final cost is calculated and persisted
        Rental completed = rentalRepository.findById(created.id());
        assertThat(completed.finalCost()).isNotNull();
    }
}