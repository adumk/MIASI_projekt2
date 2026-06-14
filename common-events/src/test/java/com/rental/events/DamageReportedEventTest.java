package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DamageReportedEventTest {

    @Test
    @DisplayName("Should correctly create DamageReportedEvent with all fields")
    void shouldCreateDamageReportedEvent() {
        // Arrange
        String vehicleId = "VEH-123";
        String description = "Scratched front bumper";
        String severity = "LOW";

        // Act
        DamageReportedEvent event = new DamageReportedEvent(vehicleId, description, severity);

        // Assert
        assertThat(event.getEventType()).isEqualTo("DamageReported");
        assertThat(event.getVehicleId()).isEqualTo(vehicleId);
        assertThat(event.getDescription()).isEqualTo(description);
        assertThat(event.getSeverity()).isEqualTo(severity);

        // Weryfikacja dziedziczenia
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}