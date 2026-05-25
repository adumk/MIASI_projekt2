package com.rental.events;

public class RentalCancelledEvent extends IntegrationEvent {

    private final String rentalId;
    private final String vehicleId;

    public RentalCancelledEvent(String rentalId, String vehicleId) {
        super("RentalCancelled");
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
    }

    public String getRentalId() { return rentalId; }
    public String getVehicleId() { return vehicleId; }
}
