package com.rental.domain;

public final class RentalCancelled extends DomainEvent {

    private final RentalId rentalId;
    private final VehicleId vehicleId;

    public RentalCancelled(RentalId rentalId, VehicleId vehicleId) {
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
    }

    public RentalId getRentalId() {
        return rentalId;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }
}
