package com.rental.ports.out;

import com.rental.domain.DateRange;
import com.rental.domain.VehicleId;

public interface IFleetAvailabilityPort {

    boolean isAvailable(VehicleId vehicleId, DateRange period);
}
