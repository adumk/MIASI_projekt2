package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentConfirmedEventTest {

    @Test
    @DisplayName("Should correctly create PaymentConfirmedEvent with all fields")
    void shouldCreatePaymentConfirmedEvent() {
        // Arrange
        String rentalId = "RENT-777";
        String customerId = "CUST-55";
        long amount = 12500L;
        String currency = "PLN";

        // Act
        PaymentConfirmedEvent event = new PaymentConfirmedEvent(rentalId, customerId, amount, currency);

        // Assert
        assertThat(event.getEventType()).isEqualTo("PaymentConfirmed");
        assertThat(event.getRentalId()).isEqualTo(rentalId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getAmountMinorUnits()).isEqualTo(amount);
        assertThat(event.getCurrency()).isEqualTo(currency);

        // Weryfikacja dziedziczenia
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}