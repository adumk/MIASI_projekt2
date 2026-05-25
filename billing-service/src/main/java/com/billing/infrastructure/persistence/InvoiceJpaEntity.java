package com.billing.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class InvoiceJpaEntity {

    @Id
    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;

    @Column(name = "rental_id", nullable = false, unique = true)
    private String rentalId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "rental_days", nullable = false)
    private int rentalDays;

    @Column(name = "vehicle_category", nullable = false)
    private String vehicleCategory;

    @Column(name = "status", nullable = false)
    private String status;

    protected InvoiceJpaEntity() {
    }

    public InvoiceJpaEntity(
            String invoiceId,
            String rentalId,
            String customerId,
            long amount,
            String currency,
            int rentalDays,
            String vehicleCategory,
            String status) {
        this.invoiceId = invoiceId;
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.rentalDays = rentalDays;
        this.vehicleCategory = vehicleCategory;
        this.status = status;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getRentalId() {
        return rentalId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public String getVehicleCategory() {
        return vehicleCategory;
    }

    public String getStatus() {
        return status;
    }
}
