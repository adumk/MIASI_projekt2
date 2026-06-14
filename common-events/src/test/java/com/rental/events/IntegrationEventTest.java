package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationEventTest {

    // Klasa pomocnicza do testowania klasy abstrakcyjnej
    private static class TestEvent extends IntegrationEvent {
        public TestEvent(String eventType) {
            super(eventType);
        }
    }

    @Test
    @DisplayName("Should correctly initialize event properties")
    void shouldInitializeProperties() {
        String type = "TestEvent";

        IntegrationEvent event = new TestEvent(type);

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(event.getEventType()).isEqualTo(type);
    }

    @Test
    @DisplayName("Should generate unique IDs for different event instances")
    void shouldGenerateUniqueIds() {
        IntegrationEvent event1 = new TestEvent("Type1");
        IntegrationEvent event2 = new TestEvent("Type2");

        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    @Test
    @DisplayName("Should not return null values")
    void shouldNotReturnNulls() {
        IntegrationEvent event = new TestEvent("Type");

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getEventType()).isNotNull();
    }
}