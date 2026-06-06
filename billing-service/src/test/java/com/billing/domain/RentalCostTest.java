package com.billing.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RentalCost — value object validation")
class RentalCostTest {

    @Test
    void shouldCreateValidRentalCost() {
        RentalCost cost = RentalCost.of(3, 150L, Money.pln(450));

        assertThat(cost.getRentalDays()).isEqualTo(3);
        assertThat(cost.getDailyRate()).isEqualTo(150L);
        assertThat(cost.getTotal()).isEqualTo(Money.pln(450));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldThrowExceptionForZeroOrNegativeDays(int days) {
        assertThatThrownBy(() -> RentalCost.of(days, 150L, Money.pln(0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -50L})
    void shouldThrowExceptionForZeroOrNegativeDailyRate(long rate) {
        assertThatThrownBy(() -> RentalCost.of(3, rate, Money.pln(0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForNullTotal() {
        assertThatThrownBy(() -> RentalCost.of(3, 150L, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalCostsShouldBeEqual() {
        RentalCost a = RentalCost.of(3, 150L, Money.pln(450));
        RentalCost b = RentalCost.of(3, 150L, Money.pln(450));

        assertThat(a).isEqualTo(b);
    }
}