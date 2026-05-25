package com.rental.events;

public class CarReturnedEvent extends IntegrationEvent {

    private final String rentalId;
    private final String vehicleId;
    private final String customerId;
    private final String returnDate;
    private final String periodStart;
    private final String vehicleCategory;
    private final long finalCostMinorUnits;
    private final String currency;

    public CarReturnedEvent(
            String rentalId,
            String vehicleId,
            String customerId,
            String returnDate,
            String periodStart,
            String vehicleCategory,
            long finalCostMinorUnits,
            String currency) {
        super("CarReturned");
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.returnDate = returnDate;
        this.periodStart = periodStart;
        this.vehicleCategory = vehicleCategory;
        this.finalCostMinorUnits = finalCostMinorUnits;
        this.currency = currency;
    }

    public CarReturnedEvent(String rentalId, String vehicleId, String customerId, String returnDate) {
        this(rentalId, vehicleId, customerId, returnDate, null, null, 0, "PLN");
    }

    public String getRentalId() { return rentalId; }
    public String getVehicleId() { return vehicleId; }
    public String getCustomerId() { return customerId; }
    public String getReturnDate() { return returnDate; }
    public String getPeriodStart() { return periodStart; }
    public String getVehicleCategory() { return vehicleCategory; }
    public long getFinalCostMinorUnits() { return finalCostMinorUnits; }
    public String getCurrency() { return currency; }
}
