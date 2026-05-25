package com.customer.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {

    private final CustomerId customerId;
    private final PersonName fullName;
    private final Email email;
    private final DriverLicense driverLicense;
    private CustomerStatus status;
    private boolean verified;
    private String blockReason;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Customer(
            CustomerId customerId,
            PersonName fullName,
            Email email,
            DriverLicense driverLicense,
            CustomerStatus status,
            boolean verified,
            String blockReason) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.driverLicense = driverLicense;
        this.status = status;
        this.verified = verified;
        this.blockReason = blockReason;
    }

    public static Customer create(
            CustomerId customerId,
            PersonName fullName,
            Email email,
            DriverLicense driverLicense) {
        return new Customer(
                customerId, fullName, email, driverLicense, CustomerStatus.ACTIVE, false, null);
    }

    public static Customer reconstitute(
            CustomerId customerId,
            PersonName fullName,
            Email email,
            DriverLicense driverLicense,
            CustomerStatus status,
            boolean verified,
            String blockReason) {
        return new Customer(customerId, fullName, email, driverLicense, status, verified, blockReason);
    }

    public void register() {
        if (status != CustomerStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only ACTIVE customers can be registered");
        }
        registerEvent(new CustomerRegistered(customerId, email));
    }

    public void verify() {
        if (status != CustomerStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only ACTIVE customers can be verified");
        }
        if (verified) {
            throw new InvalidStatusTransitionException("Customer is already verified");
        }
        verified = true;
        registerEvent(new CustomerVerified(customerId));
    }

    public void block(String reason) {
        if (status != CustomerStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only ACTIVE customers can be blocked");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("block reason must not be blank");
        }
        status = CustomerStatus.BLOCKED;
        blockReason = reason.trim();
    }

    public boolean canRent() {
        return status == CustomerStatus.ACTIVE
                && verified
                && driverLicense.isValidOn(LocalDate.now());
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public PersonName getFullName() {
        return fullName;
    }

    public Email getEmail() {
        return email;
    }

    public DriverLicense getDriverLicense() {
        return driverLicense;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getBlockReason() {
        return blockReason;
    }
}
