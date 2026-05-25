package com.fleet.application;

import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;

public record UpdateVehicleStatusCommand(VehicleId vehicleId, VehicleStatus status) {
}
