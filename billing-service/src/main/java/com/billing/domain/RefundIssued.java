package com.billing.domain;

public final class RefundIssued extends DomainEvent {

    private final RentalId rentalId;
    private final CustomerId customerId;
    private final Money amount;

    public RefundIssued(RentalId rentalId, CustomerId customerId, Money amount) {
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.amount = amount;
    }

    public RentalId getRentalId() {
        return rentalId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getAmount() {
        return amount;
    }
}
