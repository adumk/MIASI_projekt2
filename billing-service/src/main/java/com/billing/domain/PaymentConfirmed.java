package com.billing.domain;

public final class PaymentConfirmed extends DomainEvent {

    private final PaymentId paymentId;
    private final InvoiceId invoiceId;
    private final RentalId rentalId;
    private final Money amount;

    public PaymentConfirmed(
            PaymentId paymentId,
            InvoiceId invoiceId,
            RentalId rentalId,
            Money amount) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.rentalId = rentalId;
        this.amount = amount;
    }

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public InvoiceId getInvoiceId() {
        return invoiceId;
    }

    public RentalId getRentalId() {
        return rentalId;
    }

    public Money getAmount() {
        return amount;
    }
}
