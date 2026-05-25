package com.billing.adapters.out.resolvers;

import com.billing.domain.VehicleCategory;
import com.billing.ports.out.IVehicleCategoryResolver;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HardcodedVehicleCategoryResolver implements IVehicleCategoryResolver {

    private static final Map<String, VehicleCategory> BY_VEHICLE_ID = Map.of(
            "vehicle-001", VehicleCategory.STANDARD,
            "vehicle-economy", VehicleCategory.ECONOMY,
            "vehicle-standard", VehicleCategory.STANDARD,
            "vehicle-premium", VehicleCategory.PREMIUM,
            "vehicle-suv", VehicleCategory.SUV);

    @Override
    public VehicleCategory resolve(String vehicleId) {
        return BY_VEHICLE_ID.getOrDefault(vehicleId, VehicleCategory.ECONOMY);
    }
}
