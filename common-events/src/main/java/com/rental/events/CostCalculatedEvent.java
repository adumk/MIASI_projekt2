package com.rental.events;

public class CostCalculatedEvent extends IntegrationEvent {

    private final String rentalId;
    private final String customerId;
    private final long amount;
    private final String currency;

    public CostCalculatedEvent(String rentalId, String customerId, long amount, String currency) {
        super("CostCalculated");
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
    }

    public String getRentalId() { return rentalId; }
    public String getCustomerId() { return customerId; }
    public long getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
