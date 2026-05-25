package com.rental.application;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.VehicleId;

public record CreateReservationCommand(
        CustomerId customerId,
        VehicleId vehicleId,
        DateRange period) {
}
