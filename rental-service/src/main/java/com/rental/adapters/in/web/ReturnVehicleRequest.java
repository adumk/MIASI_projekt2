package com.rental.adapters.in.web;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReturnVehicleRequest(
        @NotNull LocalDate actualReturnDate,
        Integer mileage,
        String inspectionNotes) {
}
