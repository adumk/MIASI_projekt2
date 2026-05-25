package com.fleet.domain;

public final class MaintenanceScheduled extends DomainEvent {

    private final VehicleId vehicleId;

    public MaintenanceScheduled(VehicleId vehicleId) {
        this.vehicleId = vehicleId;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }
}
