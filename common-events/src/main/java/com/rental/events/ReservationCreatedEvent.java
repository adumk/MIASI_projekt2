package com.rental.events;

public class ReservationCreatedEvent extends IntegrationEvent {

    private final String rentalId;
    private final String vehicleId;
    private final String customerId;
    private final String periodStart;
    private final String periodEnd;

    public ReservationCreatedEvent(
            String rentalId, String vehicleId, String customerId, String periodStart, String periodEnd) {
        super("ReservationCreated");
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public String getRentalId() { return rentalId; }
    public String getVehicleId() { return vehicleId; }
    public String getCustomerId() { return customerId; }
    public String getPeriodStart() { return periodStart; }
    public String getPeriodEnd() { return periodEnd; }
}
