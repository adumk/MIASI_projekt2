package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarRentedEventTest {

    @Test
    @DisplayName("Should correctly create CarRentedEvent with all fields")
    void shouldCreateCarRentedEvent() {
        // Arrange
        String rentalId = "RENT-777";
        String vehicleId = "VEH-111";
        String customerId = "CUST-55";
        String startDate = "2026-06-14";

        // Act
        CarRentedEvent event = new CarRentedEvent(rentalId, vehicleId, customerId, startDate);

        // Assert
        assertThat(event.getEventType()).isEqualTo("CarRented");
        assertThat(event.getRentalId()).isEqualTo(rentalId);
        assertThat(event.getVehicleId()).isEqualTo(vehicleId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getActualStartDate()).isEqualTo(startDate);

        // Weryfikacja dziedziczenia
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}