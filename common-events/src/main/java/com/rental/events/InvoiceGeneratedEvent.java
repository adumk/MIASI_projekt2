package com.rental.events;

public class InvoiceGeneratedEvent extends IntegrationEvent {

    private final String rentalId;
    private final String customerId;

    public InvoiceGeneratedEvent(String rentalId, String customerId) {
        super("InvoiceGenerated");
        this.rentalId = rentalId;
        this.customerId = customerId;
    }

    public String getRentalId() { return rentalId; }
    public String getCustomerId() { return customerId; }
}
