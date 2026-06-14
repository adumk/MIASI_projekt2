package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationCreatedEventTest {

    @Test
    @DisplayName("Should correctly create ReservationCreatedEvent with all fields")
    void shouldCreateReservationCreatedEvent() {
        // Arrange
        String rentalId = "RENT-001";
        String vehicleId = "VEH-555";
        String customerId = "CUST-99";
        String start = "2026-06-15";
        String end = "2026-06-20";

        // Act
        ReservationCreatedEvent event = new ReservationCreatedEvent(
                rentalId, vehicleId, customerId, start, end);

        // Assert
        assertThat(event.getEventType()).isEqualTo("ReservationCreated");
        assertThat(event.getRentalId()).isEqualTo(rentalId);
        assertThat(event.getVehicleId()).isEqualTo(vehicleId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getPeriodStart()).isEqualTo(start);
        assertThat(event.getPeriodEnd()).isEqualTo(end);

        // Sprawdzenie dziedziczenia
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}