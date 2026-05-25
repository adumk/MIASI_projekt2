package com.fleet.domain;

public final class VehicleStatusChanged extends DomainEvent {

    private final VehicleId vehicleId;
    private final VehicleStatus previousStatus;
    private final VehicleStatus newStatus;

    public VehicleStatusChanged(VehicleId vehicleId, VehicleStatus previousStatus, VehicleStatus newStatus) {
        this.vehicleId = vehicleId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public VehicleStatus getPreviousStatus() {
        return previousStatus;
    }

    public VehicleStatus getNewStatus() {
        return newStatus;
    }
}
