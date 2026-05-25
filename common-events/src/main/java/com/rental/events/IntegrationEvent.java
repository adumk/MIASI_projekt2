package com.rental.events;

import java.time.Instant;
import java.util.UUID;

public abstract class IntegrationEvent {

    private final String eventId = UUID.randomUUID().toString();
    private final Instant occurredAt = Instant.now();
    private final String eventType;

    protected IntegrationEvent(String eventType) {
        this.eventType = eventType;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getEventType() {
        return eventType;
    }
}
