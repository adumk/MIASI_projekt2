package com.billing.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Invoice {

    private final InvoiceId invoiceId;
    private final RentalId rentalId;
    private final CustomerId customerId;
    private VehicleCategory vehicleCategory;
    private RentalCost rentalCost;
    private InvoiceStatus status;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Invoice(
            InvoiceId invoiceId,
            RentalId rentalId,
            CustomerId customerId,
            VehicleCategory vehicleCategory,
            RentalCost rentalCost,
            InvoiceStatus status) {
        this.invoiceId = invoiceId;
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.vehicleCategory = vehicleCategory;
        this.rentalCost = rentalCost;
        this.status = status;
    }

    public static Invoice createDraft(RentalId rentalId, CustomerId customerId, VehicleCategory category) {
        return new Invoice(
                InvoiceId.generate(),
                rentalId,
                customerId,
                category,
                null,
                InvoiceStatus.DRAFT);
    }

    public static Invoice reconstitute(
            InvoiceId invoiceId,
            RentalId rentalId,
            CustomerId customerId,
            VehicleCategory vehicleCategory,
            RentalCost rentalCost,
            InvoiceStatus status) {
        return new Invoice(invoiceId, rentalId, customerId, vehicleCategory, rentalCost, status);
    }

    public void calculateCost(RentalCost cost) {
        if (status != InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStateException("Cost can only be calculated for DRAFT invoices");
        }
        rentalCost = cost;
        status = InvoiceStatus.COST_CALCULATED;
        registerEvent(new CostCalculated(rentalId, customerId, rentalCost));
    }

    public void issue() {
        if (status != InvoiceStatus.COST_CALCULATED) {
            throw new InvalidInvoiceStateException("Invoice can only be issued after cost calculation");
        }
        status = InvoiceStatus.ISSUED;
        registerEvent(new InvoiceGenerated(invoiceId, rentalId, customerId, rentalCost));
    }

    public void markPaid() {
        if (status != InvoiceStatus.ISSUED) {
            throw new InvalidInvoiceStateException("Only ISSUED invoices can be marked as paid");
        }
        status = InvoiceStatus.PAID;
    }

    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public void issueRefund() {
        if (status != InvoiceStatus.PAID) {
            throw new InvalidInvoiceStateException("Refund can only be issued for PAID invoices");
        }
        status = InvoiceStatus.REFUNDED;
        registerEvent(new RefundIssued(
                rentalId, customerId, rentalCost != null ? rentalCost.getTotal() : Money.of(0, "PLN")));
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

    public InvoiceId getInvoiceId() {
        return invoiceId;
    }

    public RentalId getRentalId() {
        return rentalId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }

    public RentalCost getRentalCost() {
        return rentalCost;
    }

    public InvoiceStatus getStatus() {
        return status;
    }
}
