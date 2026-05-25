package com.fleet.infrastructure;

import com.fleet.domain.VehicleCategory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/** Lokalna kopia taryf (zsynchronizowana z billing TariffRegistry przy starcie). */
@Component
public class VehiclePricingService {

    private final Map<VehicleCategory, Long> dailyRates = new EnumMap<>(VehicleCategory.class);

    public VehiclePricingService() {
        dailyRates.put(VehicleCategory.ECONOMY, 100L);
        dailyRates.put(VehicleCategory.STANDARD, 150L);
        dailyRates.put(VehicleCategory.PREMIUM, 250L);
        dailyRates.put(VehicleCategory.SUV, 300L);
        dailyRates.put(VehicleCategory.VAN, 200L);
    }

    public long dailyRateMinorUnits(VehicleCategory category) {
        return dailyRates.getOrDefault(category, 100L);
    }
}
