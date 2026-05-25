package com.customer.application;

import java.time.LocalDate;

public record RegisterWithPasswordCommand(
        String firstName,
        String lastName,
        String email,
        String password,
        String licenseNumber,
        LocalDate licenseExpiryDate) {}
