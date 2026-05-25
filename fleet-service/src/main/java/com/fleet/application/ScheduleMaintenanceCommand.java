package com.fleet.application;

import com.fleet.domain.VehicleId;

public record ScheduleMaintenanceCommand(VehicleId vehicleId) {
}
