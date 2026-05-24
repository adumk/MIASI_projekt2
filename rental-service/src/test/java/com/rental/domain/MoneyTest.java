package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money — value object contract")
class MoneyTest {

    private static final Currency PLN = Currency.getInstance("PLN");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    @DisplayName("should create Money for a non-negative amount")
    void shouldCreateMoneyForNonNegativeAmount() {
        Money money = Money.of(new BigDecimal("350.00"), PLN);

        assertThat(money).isNotNull();
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(money.currency()).isEqualTo(PLN);
    }

    @Test
    @DisplayName("should create Money for zero amount as a boundary case")
    void shouldCreateMoneyForZeroAmount() {
        Money money = Money.of(BigDecimal.ZERO, PLN);

        assertThat(money).isNotNull();
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("should reject negative amount")
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-0.01"), PLN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should reject null currency")
    void shouldRejectNullCurrency() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("100.00"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should reject null amount")
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> Money.of(null, PLN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should consider two Money instances with the same amount and currency as equal")
    void shouldBeEqualForTheSameAmountAndCurrency() {
        Money first  = Money.of(new BigDecimal("200.00"), PLN);
        Money second = Money.of(new BigDecimal("200.00"), PLN);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("should not consider Money instances with different currencies as equal")
    void shouldNotBeEqualForDifferentCurrencies() {
        Money pln = Money.of(new BigDecimal("200.00"), PLN);
        Money eur = Money.of(new BigDecimal("200.00"), EUR);

        assertThat(pln).isNotEqualTo(eur);
    }

    @Test
    @DisplayName("should have consistent hashCode for equal Money instances")
    void shouldHaveConsistentHashCode() {
        Money first  = Money.of(new BigDecimal("200.00"), PLN);
        Money second = Money.of(new BigDecimal("200.00"), PLN);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("should expose a readable toString containing amount and currency")
    void shouldExposeReadableToString() {
        Money money = Money.of(new BigDecimal("350.00"), PLN);
        String result = money.toString();

        assertThat(result).isNotBlank();
        assertThat(result).contains("350");
        assertThat(result).contains("PLN");
    }
}