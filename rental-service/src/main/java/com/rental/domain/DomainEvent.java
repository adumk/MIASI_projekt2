package com.rental.domain;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final int version = 1;

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public int getVersion() {
        return version;
    }
}
