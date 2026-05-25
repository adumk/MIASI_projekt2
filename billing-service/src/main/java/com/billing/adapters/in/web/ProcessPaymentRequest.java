package com.billing.adapters.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProcessPaymentRequest(
        @NotBlank String rentalId,
        @Min(1) long amount,
        @NotBlank String currency) {
}
