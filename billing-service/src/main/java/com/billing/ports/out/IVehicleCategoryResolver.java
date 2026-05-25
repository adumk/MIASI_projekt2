package com.billing.ports.out;

import com.billing.domain.VehicleCategory;

public interface IVehicleCategoryResolver {

    VehicleCategory resolve(String vehicleId);
}
