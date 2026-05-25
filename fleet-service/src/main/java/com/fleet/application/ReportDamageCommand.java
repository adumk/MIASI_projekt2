package com.fleet.application;

import com.fleet.domain.DamageSeverity;
import com.fleet.domain.VehicleId;

public record ReportDamageCommand(VehicleId vehicleId, String description, DamageSeverity severity) {
}
