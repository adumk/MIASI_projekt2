package com.fleet.application;

import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;

public record AddVehicleCommand(
        VehicleId vehicleId,
        String licensePlate,
        String brand,
        String model,
        int year,
        VehicleCategory category) {
}
