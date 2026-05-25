package com.fleet.domain;

public final class VehicleAdded extends DomainEvent {

    private final VehicleId vehicleId;
    private final String licensePlate;
    private final String brand;
    private final String model;
    private final int year;
    private final VehicleCategory category;

    public VehicleAdded(
            VehicleId vehicleId,
            String licensePlate,
            String brand,
            String model,
            int year,
            VehicleCategory category) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.category = category;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public VehicleCategory getCategory() {
        return category;
    }
}
