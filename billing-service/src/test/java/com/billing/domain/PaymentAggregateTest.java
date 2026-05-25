package com.billing.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment aggregate — confirmation and domain event emission")
class PaymentAggregateTest {

    @Test
    @DisplayName("Should confirm payment and emit PaymentConfirmed event")
    void shouldConfirmPayment() {
        Payment payment = Payment.create(
                InvoiceId.of("invoice-001"),
                RentalId.of("rental-001"),
                Money.pln(450));

        payment.confirm();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(payment.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(PaymentConfirmed.class);
    }

    @Test
    @DisplayName("Should reject confirmation when payment is already confirmed")
    void shouldRejectDoubleConfirmation() {
        Payment payment = Payment.create(
                InvoiceId.of("invoice-001"),
                RentalId.of("rental-001"),
                Money.pln(450));
        payment.confirm();

        assertThatThrownBy(payment::confirm)
                .isInstanceOf(IllegalStateException.class);
    }
}
