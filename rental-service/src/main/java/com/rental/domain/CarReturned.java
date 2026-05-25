package com.rental.domain;

import java.time.LocalDate;

public final class CarReturned extends DomainEvent {

    private final RentalId rentalId;
    private final VehicleId vehicleId;
    private final CustomerId customerId;
    private final LocalDate returnDate;
    private final LocalDate periodStart;
    private final Money finalCost;

    public CarReturned(
            RentalId rentalId,
            VehicleId vehicleId,
            CustomerId customerId,
            LocalDate returnDate,
            LocalDate periodStart,
            Money finalCost) {
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.returnDate = returnDate;
        this.periodStart = periodStart;
        this.finalCost = finalCost;
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

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public Money getFinalCost() {
        return finalCost;
    }
}
