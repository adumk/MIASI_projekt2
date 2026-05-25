package com.customer.adapters.in.web;

import com.customer.domain.Customer;

import java.time.LocalDate;

public record CustomerResponse(
        String customerId,
        String firstName,
        String lastName,
        String email,
        String licenseNumber,
        LocalDate licenseExpiryDate,
        String status,
        boolean verified,
        String blockReason) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId().getValue(),
                customer.getFullName().getFirstName(),
                customer.getFullName().getLastName(),
                customer.getEmail().getValue(),
                customer.getDriverLicense().getNumber(),
                customer.getDriverLicense().getExpiryDate(),
                customer.getStatus().name(),
                customer.isVerified(),
                customer.getBlockReason());
    }
}
