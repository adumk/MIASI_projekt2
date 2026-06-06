package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money — value object validation and equality")
class MoneyTest {

    @Test
    void shouldCreateMoneyWithPositiveAmount() {
        Money money = Money.of(350, "PLN");

        assertThat(money.getAmount().longValue()).isEqualTo(350);
        assertThat(money.getCurrency()).isEqualTo("PLN");
    }

    @Test
    void shouldThrowExceptionForNegativeAmount() {
        assertThatThrownBy(() -> Money.of(-1, "PLN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void shouldThrowExceptionForZeroAmount() {
        Money money = Money.of(0, "PLN");

        assertThat(money.getAmount().longValue()).isEqualTo(0);
    }

    @Test
    void shouldThrowExceptionForNullCurrency() {
        assertThatThrownBy(() -> Money.of(100, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalMoneyObjectsShouldBeEqual() {
        Money a = Money.of(100, "PLN");
        Money b = Money.of(100, "PLN");

        assertThat(a).isEqualTo(b);
    }

    @Test
    void differentAmountsShouldNotBeEqual() {
        Money a = Money.of(100, "PLN");
        Money b = Money.of(200, "PLN");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void differentCurrenciesShouldNotBeEqual() {
        Money a = Money.of(100, "PLN");
        Money b = Money.of(100, "EUR");

        assertThat(a).isNotEqualTo(b);
    }
}