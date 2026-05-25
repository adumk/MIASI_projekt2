package com.customer.application;

import com.customer.domain.Customer;

public record AuthSession(
        String customerId,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean verified,
        String token) {

    public static AuthSession from(Customer customer, String role, String token) {
        return new AuthSession(
                customer.getCustomerId().getValue(),
                customer.getEmail().getValue(),
                customer.getFullName().getFirstName(),
                customer.getFullName().getLastName(),
                role,
                customer.isVerified(),
                token);
    }
}
