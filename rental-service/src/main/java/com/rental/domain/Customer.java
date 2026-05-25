package com.rental.domain;

public final class Customer {

    private final CustomerId customerId;
    private final boolean eligible;

    private Customer(CustomerId customerId, boolean eligible) {
        this.customerId = customerId;
        this.eligible = eligible;
    }

    public static Customer eligible(CustomerId customerId) {
        return new Customer(customerId, true);
    }

    public static Customer blocked(CustomerId customerId) {
        return new Customer(customerId, false);
    }

    public boolean canRent() {
        return eligible;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }
}
