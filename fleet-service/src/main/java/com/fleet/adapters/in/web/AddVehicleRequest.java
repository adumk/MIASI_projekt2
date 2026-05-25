package com.fleet.adapters.in.web;

import com.fleet.domain.VehicleCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddVehicleRequest(
        String vehicleId,
        @NotBlank String licensePlate,
        @NotBlank String brand,
        @NotBlank String model,
        @Min(1900) int year,
        @NotNull VehicleCategory category) {
}
