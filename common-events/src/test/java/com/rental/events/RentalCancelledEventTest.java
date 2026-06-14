package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RentalCancelledEventTest {

    @Test
    @DisplayName("Should correctly create RentalCancelledEvent with all fields")
    void shouldCreateRentalCancelledEvent() {
        // Arrange
        String rentalId = "RENT-999";
        String vehicleId = "VEH-123";

        // Act
        RentalCancelledEvent event = new RentalCancelledEvent(rentalId, vehicleId);

        // Assert
        assertThat(event.getEventType()).isEqualTo("RentalCancelled");
        assertThat(event.getRentalId()).isEqualTo(rentalId);
        assertThat(event.getVehicleId()).isEqualTo(vehicleId);

        // Weryfikacja dziedziczenia
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}