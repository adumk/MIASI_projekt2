package com.customer.domain;

public final class CustomerVerified extends DomainEvent {

    private final CustomerId customerId;

    public CustomerVerified(CustomerId customerId) {
        this.customerId = customerId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
}
