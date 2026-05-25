package com.rental.domain;

public final class ReservationCreated extends DomainEvent {

    private final RentalId rentalId;
    private final VehicleId vehicleId;
    private final CustomerId customerId;
    private final DateRange period;

    public ReservationCreated(RentalId rentalId, VehicleId vehicleId, CustomerId customerId, DateRange period) {
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.period = period;
    }

    public RentalId getRentalId() {
        return rentalId;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public DateRange getPeriod() {
        return period;
    }
}
