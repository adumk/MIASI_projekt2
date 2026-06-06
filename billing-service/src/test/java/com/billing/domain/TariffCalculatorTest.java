package com.billing.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TariffCalculator — daily rates by vehicle category")
class TariffCalculatorTest {

    @Test
    @DisplayName("Should calculate rental cost using category daily rate and rental days")
    void shouldCalculateRentalCost() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 4);

        RentalCost cost = TariffCalculator.calculate(VehicleCategory.SUV, start, end);

        assertThat(cost.getRentalDays()).isEqualTo(3);
        assertThat(cost.getDailyRate()).isEqualTo(300L);
        assertThat(cost.getTotal()).isEqualTo(Money.pln(900));
    }

    @Test
    void shouldCalculateRentalCostForEconomy() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 3);

        RentalCost cost = TariffCalculator.calculate(VehicleCategory.ECONOMY, start, end);

        assertThat(cost.getRentalDays()).isEqualTo(2);
        assertThat(cost.getDailyRate()).isEqualTo(100L);
        assertThat(cost.getTotal()).isEqualTo(Money.pln(200));
    }

    @Test
    void shouldCalculateRentalCostForStandard() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 6);

        RentalCost cost = TariffCalculator.calculate(VehicleCategory.STANDARD, start, end);

        assertThat(cost.getRentalDays()).isEqualTo(5);
        assertThat(cost.getDailyRate()).isEqualTo(150L);
        assertThat(cost.getTotal()).isEqualTo(Money.pln(750));
    }

    @Test
    void shouldCalculateRentalCostForPremium() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 2);

        RentalCost cost = TariffCalculator.calculate(VehicleCategory.PREMIUM, start, end);

        assertThat(cost.getRentalDays()).isEqualTo(1);
        assertThat(cost.getDailyRate()).isEqualTo(250L);
        assertThat(cost.getTotal()).isEqualTo(Money.pln(250));
    }

    @Test
    void shouldCalculateRentalCostForVan() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 8);

        RentalCost cost = TariffCalculator.calculate(VehicleCategory.VAN, start, end);

        assertThat(cost.getRentalDays()).isEqualTo(7);
        assertThat(cost.getDailyRate()).isEqualTo(180L);
        assertThat(cost.getTotal()).isEqualTo(Money.pln(1260));
    }

    @Test
    void shouldCalculateMinimumOneDayEvenWhenSameDates() {
        LocalDate date = LocalDate.of(2026, 5, 1);

        RentalCost cost = TariffCalculator.calculate(VehicleCategory.ECONOMY, date, date);

        assertThat(cost.getRentalDays()).isEqualTo(1);
    }

    @Test
    void shouldCalculateWithExtraDamageFee() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 4);

        RentalCost cost = TariffCalculator.calculate(VehicleCategory.SUV, start, end, 5000L);

        assertThat(cost.getTotal()).isEqualTo(Money.pln(5900));
    }

    @Test
    void damageFeeForMinorSeverityShouldBe5000() {
        assertThat(TariffCalculator.damageFee(DamageSeverity.MINOR)).isEqualTo(5000L);
    }

    @Test
    void damageFeeForModerateSeverityShouldBe15000() {
        assertThat(TariffCalculator.damageFee(DamageSeverity.MODERATE)).isEqualTo(15000L);
    }

    @Test
    void damageFeeForSevereSeverityShouldBe50000() {
        assertThat(TariffCalculator.damageFee(DamageSeverity.SEVERE)).isEqualTo(50000L);
    }
}
