package com.billing.domain;

public final class InvoiceGenerated extends DomainEvent {

    private final InvoiceId invoiceId;
    private final RentalId rentalId;
    private final CustomerId customerId;
    private final RentalCost rentalCost;

    public InvoiceGenerated(
            InvoiceId invoiceId,
            RentalId rentalId,
            CustomerId customerId,
            RentalCost rentalCost) {
        this.invoiceId = invoiceId;
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.rentalCost = rentalCost;
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

    public RentalCost getRentalCost() {
        return rentalCost;
    }
}
