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
}
