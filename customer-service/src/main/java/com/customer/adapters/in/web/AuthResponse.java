package com.customer.adapters.in.web;

import com.customer.application.AuthSession;

public record AuthResponse(
        String customerId,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean verified,
        String accessToken) {

    public static AuthResponse from(AuthSession session) {
        return new AuthResponse(
                session.customerId(),
                session.email(),
                session.firstName(),
                session.lastName(),
                session.role(),
                session.verified(),
                session.token());
    }
}
