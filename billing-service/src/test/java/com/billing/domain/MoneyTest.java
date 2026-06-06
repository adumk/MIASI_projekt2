package com.billing.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money (billing) — factory methods and equality")
class MoneyTest {

    @Test
    void plnFactoryMethodShouldSetPLNCurrency() {
        Money money = Money.pln(450);

        assertThat(money.getCurrency()).isEqualTo("PLN");
        assertThat(money.getAmount().longValue()).isEqualTo(450);
    }

    @Test
    void toMinorUnitsShouldReturnLongValue() {
        Money money = Money.pln(350);

        assertThat(money.toMinorUnits()).isEqualTo(350L);
    }

    @Test
    void shouldThrowExceptionForNegativeAmount() {
        assertThatThrownBy(() -> Money.of(-1, "PLN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalMoneyObjectsShouldBeEqual() {
        Money a = Money.pln(100);
        Money b = Money.pln(100);

        assertThat(a).isEqualTo(b);
    }

    @Test
    void moneyWithDifferentAmountShouldNotBeEqual() {
        Money a = Money.pln(100);
        Money b = Money.pln(200);

        assertThat(a).isNotEqualTo(b);
    }
}