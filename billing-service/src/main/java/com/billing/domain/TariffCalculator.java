package com.billing.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class TariffCalculator {

    private TariffCalculator() {}

    public static RentalCost calculate(VehicleCategory category, LocalDate startDate, LocalDate endDate) {
        return calculate(category, startDate, endDate, 0);
    }

    public static RentalCost calculate(
            VehicleCategory category, LocalDate startDate, LocalDate endDate, long extraMinorUnits) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) {
            days = 1;
        }
        long dailyRate = TariffRegistry.getDailyRate(category);
        long total = dailyRate * days + extraMinorUnits;
        return RentalCost.of((int) days, dailyRate, Money.pln(total));
    }

    public static long damageFee(DamageSeverity severity) {
        return switch (severity) {
            case MINOR -> 50_00L;
            case MODERATE -> 150_00L;
            case SEVERE -> 500_00L;
        };
    }
}
