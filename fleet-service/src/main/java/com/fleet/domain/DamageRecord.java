package com.fleet.domain;

import java.time.Instant;
import java.util.Objects;

public final class DamageRecord {

    private final String description;
    private final DamageSeverity severity;
    private final Instant reportedAt;

    public DamageRecord(String description, DamageSeverity severity) {
        this(description, severity, Instant.now());
    }

    public DamageRecord(String description, DamageSeverity severity, Instant reportedAt) {
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.reportedAt = Objects.requireNonNull(reportedAt, "reportedAt must not be null");
    }

    public String getDescription() {
        return description;
    }

    public DamageSeverity getSeverity() {
        return severity;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }
}
