package com.billing.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Payment {

    private final PaymentId paymentId;
    private final InvoiceId invoiceId;
    private final RentalId rentalId;
    private final Money amount;
    private PaymentStatus status;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Payment(
            PaymentId paymentId,
            InvoiceId invoiceId,
            RentalId rentalId,
            Money amount,
            PaymentStatus status) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.rentalId = rentalId;
        this.amount = amount;
        this.status = status;
    }

    public static Payment create(InvoiceId invoiceId, RentalId rentalId, Money amount) {
        return new Payment(PaymentId.generate(), invoiceId, rentalId, amount, PaymentStatus.PENDING);
    }

    public static Payment reconstitute(
            PaymentId paymentId,
            InvoiceId invoiceId,
            RentalId rentalId,
            Money amount,
            PaymentStatus status) {
        return new Payment(paymentId, invoiceId, rentalId, amount, status);
    }

    public void confirm() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment can only be confirmed from PENDING status");
        }
        status = PaymentStatus.CONFIRMED;
        registerEvent(new PaymentConfirmed(paymentId, invoiceId, rentalId, amount));
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
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

    public PaymentStatus getStatus() {
        return status;
    }
}
