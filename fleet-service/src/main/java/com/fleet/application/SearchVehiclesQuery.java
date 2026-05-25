package com.fleet.application;

import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleStatus;

public record SearchVehiclesQuery(VehicleStatus status, VehicleCategory category) {
}
