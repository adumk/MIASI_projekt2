package com.fleet.domain;

public final class DamageReported extends DomainEvent {

    private final VehicleId vehicleId;
    private final String description;
    private final DamageSeverity severity;

    public DamageReported(VehicleId vehicleId, String description, DamageSeverity severity) {
        this.vehicleId = vehicleId;
        this.description = description;
        this.severity = severity;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public String getDescription() {
        return description;
    }

    public DamageSeverity getSeverity() {
        return severity;
    }
}
