package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CostCalculatedEventTest {

    @Test
    @DisplayName("Should correctly create CostCalculatedEvent with all fields")
    void shouldCreateCostCalculatedEvent() {
        // Arrange
        String rentalId = "RENT-444";
        String customerId = "CUST-10";
        long amount = 15000L;
        String currency = "PLN";

        // Act
        CostCalculatedEvent event = new CostCalculatedEvent(rentalId, customerId, amount, currency);

        // Assert
        assertThat(event.getEventType()).isEqualTo("CostCalculated");
        assertThat(event.getRentalId()).isEqualTo(rentalId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getAmount()).isEqualTo(amount);
        assertThat(event.getCurrency()).isEqualTo(currency);

        // Weryfikacja dziedziczenia
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}