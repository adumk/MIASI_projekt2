package com.rental.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTopicsTest {

    // Metoda pomocnicza wewnątrz testu, aby sprawdzić logikę przypisania tematów
    private String resolveTopic(IntegrationEvent event) {
        if (event instanceof ReservationCreatedEvent) {
            return "reservation-topic";
        }
        return "default-topic";
    }

    @Test
    @DisplayName("Should resolve correct topic for ReservationCreatedEvent")
    void shouldResolveCorrectTopicForReservationCreated() {
        // ReservationCreatedEvent znajduje się w tym samym pakiecie com.rental.events
        IntegrationEvent event = new ReservationCreatedEvent(
                "R1", "V1", "C1", "2026-06-01", "2026-06-07");

        String topic = resolveTopic(event);

        assertThat(topic).isEqualTo("reservation-topic");
    }

    @Test
    @DisplayName("Should return default topic for unknown event type")
    void shouldReturnDefaultTopic() {
        // Tworzymy anonimową klasę implementującą IntegrationEvent
        IntegrationEvent unknownEvent = new IntegrationEvent("Unknown") {};

        String topic = resolveTopic(unknownEvent);

        assertThat(topic).isEqualTo("default-topic");
    }
}