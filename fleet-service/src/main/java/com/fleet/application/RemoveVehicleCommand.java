package com.fleet.application;

import com.fleet.domain.VehicleId;

public record RemoveVehicleCommand(VehicleId vehicleId) {
}
