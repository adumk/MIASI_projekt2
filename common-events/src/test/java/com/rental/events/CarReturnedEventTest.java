package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarReturnedEventTest {

    @Test
    @DisplayName("Should correctly create CarReturnedEvent with full constructor")
    void shouldCreateCarReturnedEventWithFullConstructor() {
        // Arrange
        String rentalId = "RENT-123";
        String vehicleId = "VEH-999";
        String customerId = "CUST-001";
        String returnDate = "2026-06-20";
        String start = "2026-06-15";
        String cat = "SUV";
        long cost = 50000L;
        String cur = "PLN";

        // Act
        CarReturnedEvent event = new CarReturnedEvent(
                rentalId, vehicleId, customerId, returnDate, start, cat, cost, cur);

        // Assert
        assertThat(event.getEventType()).isEqualTo("CarReturned");
        assertThat(event.getRentalId()).isEqualTo(rentalId);
        assertThat(event.getVehicleId()).isEqualTo(vehicleId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getReturnDate()).isEqualTo(returnDate);
        assertThat(event.getPeriodStart()).isEqualTo(start);
        assertThat(event.getVehicleCategory()).isEqualTo(cat);
        assertThat(event.getFinalCostMinorUnits()).isEqualTo(cost);
        assertThat(event.getCurrency()).isEqualTo(cur);
    }

    @Test
    @DisplayName("Should correctly create CarReturnedEvent with simplified constructor")
    void shouldCreateCarReturnedEventWithSimplifiedConstructor() {
        // Act
        CarReturnedEvent event = new CarReturnedEvent("R1", "V1", "C1", "2026-06-20");

        // Assert
        assertThat(event.getEventType()).isEqualTo("CarReturned");
        assertThat(event.getRentalId()).isEqualTo("R1");
        assertThat(event.getVehicleId()).isEqualTo("V1");
        assertThat(event.getCustomerId()).isEqualTo("C1");
        assertThat(event.getReturnDate()).isEqualTo("2026-06-20");
        assertThat(event.getCurrency()).isEqualTo("PLN"); // Domyślna wartość
        assertThat(event.getFinalCostMinorUnits()).isEqualTo(0); // Domyślna wartość
    }
}