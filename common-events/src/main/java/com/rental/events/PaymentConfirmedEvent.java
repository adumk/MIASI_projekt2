package com.rental.events;

public class PaymentConfirmedEvent extends IntegrationEvent {

    private final String rentalId;
    private final String customerId;
    private final long amountMinorUnits;
    private final String currency;

    public PaymentConfirmedEvent(
            String rentalId, String customerId, long amountMinorUnits, String currency) {
        super("PaymentConfirmed");
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.amountMinorUnits = amountMinorUnits;
        this.currency = currency;
    }

    public String getRentalId() {
        return rentalId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public long getAmountMinorUnits() {
        return amountMinorUnits;
    }

    public String getCurrency() {
        return currency;
    }
}
