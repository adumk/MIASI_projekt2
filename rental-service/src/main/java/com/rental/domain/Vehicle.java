package com.rental.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vehicle {

    private final VehicleId vehicleId;
    private VehicleStatus status;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Vehicle(VehicleId vehicleId, VehicleStatus status) {
        this.vehicleId = vehicleId;
        this.status = status;
    }

    public static Vehicle withStatus(VehicleId vehicleId, VehicleStatus status) {
        return new Vehicle(vehicleId, status);
    }

    public static Vehicle reconstitute(VehicleId vehicleId, VehicleStatus status) {
        return new Vehicle(vehicleId, status);
    }

    public void rent() {
        if (status != VehicleStatus.AVAILABLE) {
            throw new VehicleNotAvailableException("Vehicle is not available for rent");
        }
        VehicleStatus previous = status;
        status = VehicleStatus.RENTED;
        registerEvent(new VehicleStatusChanged(vehicleId, previous, status));
    }

    public void returnVehicle() {
        if (status != VehicleStatus.RENTED) {
            throw new InvalidStatusTransitionException("Only RENTED vehicles can be returned");
        }
        VehicleStatus previous = status;
        status = VehicleStatus.AVAILABLE;
        registerEvent(new VehicleStatusChanged(vehicleId, previous, status));
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

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public VehicleStatus getStatus() {
        return status;
    }
}
