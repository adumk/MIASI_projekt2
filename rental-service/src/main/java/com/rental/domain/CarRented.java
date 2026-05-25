package com.rental.domain;

import java.time.LocalDate;

public final class CarRented extends DomainEvent {

    private final RentalId rentalId;
    private final VehicleId vehicleId;
    private final CustomerId customerId;
    private final LocalDate actualStartDate;

    public CarRented(RentalId rentalId, VehicleId vehicleId, CustomerId customerId, LocalDate actualStartDate) {
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.actualStartDate = actualStartDate;
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

    public LocalDate getActualStartDate() {
        return actualStartDate;
    }
}
