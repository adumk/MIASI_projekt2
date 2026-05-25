package com.rental.events;

public class CarRentedEvent extends IntegrationEvent {

    private final String rentalId;
    private final String vehicleId;
    private final String customerId;
    private final String actualStartDate;

    public CarRentedEvent(String rentalId, String vehicleId, String customerId, String actualStartDate) {
        super("CarRented");
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.actualStartDate = actualStartDate;
    }

    public String getRentalId() { return rentalId; }
    public String getVehicleId() { return vehicleId; }
    public String getCustomerId() { return customerId; }
    public String getActualStartDate() { return actualStartDate; }
}
