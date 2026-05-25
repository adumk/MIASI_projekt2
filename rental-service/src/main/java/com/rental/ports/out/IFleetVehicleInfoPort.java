package com.rental.ports.out;

import com.rental.domain.VehicleId;

public interface IFleetVehicleInfoPort {

    String resolveCategory(VehicleId vehicleId);
}
