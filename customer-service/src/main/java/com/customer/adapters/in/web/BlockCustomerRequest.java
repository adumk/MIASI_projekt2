package com.customer.adapters.in.web;

import jakarta.validation.constraints.NotBlank;

public record BlockCustomerRequest(@NotBlank String reason) {}
