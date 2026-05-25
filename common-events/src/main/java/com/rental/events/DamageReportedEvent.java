package com.rental.events;

public class DamageReportedEvent extends IntegrationEvent {

    private final String vehicleId;
    private final String description;
    private final String severity;

    public DamageReportedEvent(String vehicleId, String description, String severity) {
        super("DamageReported");
        this.vehicleId = vehicleId;
        this.description = description;
        this.severity = severity;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }
}
