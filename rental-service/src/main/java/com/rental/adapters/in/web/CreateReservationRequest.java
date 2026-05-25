package com.rental.adapters.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateReservationRequest(
        @NotBlank String customerId,
        @NotBlank String vehicleId,
        @Email String email,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate) {
}
