package com.fleet.adapters.in.web;

import com.fleet.domain.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleStatusRequest(@NotNull VehicleStatus status) {
}
