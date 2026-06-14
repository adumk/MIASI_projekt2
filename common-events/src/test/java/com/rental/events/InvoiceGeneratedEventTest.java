package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceGeneratedEventTest {

    @Test
    @DisplayName("Should correctly create InvoiceGeneratedEvent with all fields")
    void shouldCreateInvoiceGeneratedEvent() {
        // Arrange
        String rentalId = "RENT-999";
        String customerId = "CUST-10";

        // Act
        InvoiceGeneratedEvent event = new InvoiceGeneratedEvent(rentalId, customerId);

        // Assert
        assertThat(event.getEventType()).isEqualTo("InvoiceGenerated");
        assertThat(event.getRentalId()).isEqualTo(rentalId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);

        // Weryfikacja dziedziczenia
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}