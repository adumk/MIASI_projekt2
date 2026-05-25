package com.billing.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class TariffRegistry {

    private static final Map<VehicleCategory, Long> DAILY_RATES = new EnumMap<>(VehicleCategory.class);

    static {
        DAILY_RATES.put(VehicleCategory.ECONOMY, 100L);
        DAILY_RATES.put(VehicleCategory.STANDARD, 150L);
        DAILY_RATES.put(VehicleCategory.PREMIUM, 250L);
        DAILY_RATES.put(VehicleCategory.SUV, 300L);
        DAILY_RATES.put(VehicleCategory.VAN, 180L);
    }

    private TariffRegistry() {}

    public static long getDailyRate(VehicleCategory category) {
        return DAILY_RATES.getOrDefault(category, DAILY_RATES.get(VehicleCategory.ECONOMY));
    }

    public static void setDailyRate(VehicleCategory category, long dailyRateMinorUnits) {
        if (dailyRateMinorUnits < 1) {
            throw new IllegalArgumentException("Daily rate must be positive");
        }
        DAILY_RATES.put(category, dailyRateMinorUnits);
    }

    public static Map<String, Long> snapshot() {
        return DAILY_RATES.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
    }
}
