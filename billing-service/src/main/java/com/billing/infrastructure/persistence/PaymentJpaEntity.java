package com.billing.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {

    @Id
    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;

    @Column(name = "rental_id", nullable = false)
    private String rentalId;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    private String status;

    protected PaymentJpaEntity() {
    }

    public PaymentJpaEntity(
            String paymentId,
            String invoiceId,
            String rentalId,
            long amount,
            String currency,
            String status) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.rentalId = rentalId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getRentalId() {
        return rentalId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }
}
