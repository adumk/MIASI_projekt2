package com.fleet.infrastructure.persistence;

import com.fleet.domain.DamageRecord;
import com.fleet.domain.DamageSeverity;

import java.time.Instant;

public class DamageRecordDocument {

    private String description;
    private String severity;
    private Instant reportedAt;

    public DamageRecordDocument() {
    }

    public DamageRecordDocument(String description, String severity, Instant reportedAt) {
        this.description = description;
        this.severity = severity;
        this.reportedAt = reportedAt;
    }

    public static DamageRecordDocument fromDomain(DamageRecord record) {
        return new DamageRecordDocument(
                record.getDescription(),
                record.getSeverity().name(),
                record.getReportedAt());
    }

    public DamageRecord toDomain() {
        return new DamageRecord(description, DamageSeverity.valueOf(severity), reportedAt);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Instant reportedAt) {
        this.reportedAt = reportedAt;
    }
}
