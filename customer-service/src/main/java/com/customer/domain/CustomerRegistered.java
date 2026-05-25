package com.customer.domain;

public final class CustomerRegistered extends DomainEvent {

    private final CustomerId customerId;
    private final Email email;

    public CustomerRegistered(CustomerId customerId, Email email) {
        this.customerId = customerId;
        this.email = email;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Email getEmail() {
        return email;
    }
}
