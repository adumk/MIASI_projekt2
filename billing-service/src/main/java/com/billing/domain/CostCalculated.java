package com.billing.domain;

public final class CostCalculated extends DomainEvent {

    private final RentalId rentalId;
    private final CustomerId customerId;
    private final RentalCost rentalCost;

    public CostCalculated(RentalId rentalId, CustomerId customerId, RentalCost rentalCost) {
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.rentalCost = rentalCost;
    }

    public RentalId getRentalId() {
        return rentalId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public RentalCost getRentalCost() {
        return rentalCost;
    }
}
