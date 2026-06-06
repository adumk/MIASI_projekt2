package com.billing.ports.out;

import com.billing.domain.InvoiceId;
import com.billing.domain.Money;
import com.billing.domain.Payment;
import com.billing.domain.PaymentStatus;
import com.billing.domain.RentalId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class IPaymentRepositoryContractTest {

    protected abstract IPaymentRepository getRepositoryInstance();

    @Test
    @DisplayName("Should save and find payment by rentalId with CONFIRMED status")
    void shouldSaveAndFindPayment() {
        IPaymentRepository repository = getRepositoryInstance();

        InvoiceId invoiceId = InvoiceId.of("contract-invoice-pay-001");
        RentalId rentalId = RentalId.of("contract-rental-pay-001");

        Payment payment = Payment.create(invoiceId, rentalId, Money.pln(450));
        payment.confirm();
        repository.save(payment);

        Optional<Payment> found = repository.findByRentalId(rentalId);

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(found.get().getAmount()).isEqualTo(Money.pln(450));
    }
}