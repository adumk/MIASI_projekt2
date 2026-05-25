package com.fleet.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vehicle {

    private final VehicleId vehicleId;
    private final String licensePlate;
    private final String brand;
    private final String model;
    private final int year;
    private final VehicleCategory category;
    private VehicleStatus status;
    private final List<DamageRecord> damageRecords;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Vehicle(
            VehicleId vehicleId,
            String licensePlate,
            String brand,
            String model,
            int year,
            VehicleCategory category,
            VehicleStatus status,
            List<DamageRecord> damageRecords) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.category = category;
        this.status = status;
        this.damageRecords = new ArrayList<>(damageRecords);
    }

    public static Vehicle create(
            VehicleId vehicleId,
            String licensePlate,
            String brand,
            String model,
            int year,
            VehicleCategory category) {
        Vehicle vehicle = new Vehicle(
                vehicleId,
                licensePlate,
                brand,
                model,
                year,
                category,
                VehicleStatus.AVAILABLE,
                List.of());
        vehicle.registerEvent(new VehicleAdded(vehicleId, licensePlate, brand, model, year, category));
        return vehicle;
    }

    public static Vehicle reconstitute(
            VehicleId vehicleId,
            String licensePlate,
            String brand,
            String model,
            int year,
            VehicleCategory category,
            VehicleStatus status,
            List<DamageRecord> damageRecords) {
        return new Vehicle(vehicleId, licensePlate, brand, model, year, category, status, damageRecords);
    }

    public void reserve() {
        if (status != VehicleStatus.AVAILABLE) {
            throw new InvalidStatusTransitionException("Only AVAILABLE vehicles can be reserved");
        }
        transitionTo(VehicleStatus.RESERVED);
    }

    public void releaseReservation() {
        if (status != VehicleStatus.RESERVED) {
            throw new InvalidStatusTransitionException("Only RESERVED vehicles can release reservation");
        }
        transitionTo(VehicleStatus.AVAILABLE);
    }

    public void rent() {
        if (status != VehicleStatus.AVAILABLE && status != VehicleStatus.RESERVED) {
            throw new InvalidStatusTransitionException("Only AVAILABLE or RESERVED vehicles can be rented");
        }
        transitionTo(VehicleStatus.RENTED);
    }

    public void removeFromFleet() {
        if (status == VehicleStatus.RENTED || status == VehicleStatus.RESERVED) {
            throw new InvalidStatusTransitionException("Cannot remove vehicle that is reserved or rented");
        }
    }

    public void returnVehicle() {
        if (status != VehicleStatus.RENTED) {
            throw new InvalidStatusTransitionException("Only RENTED vehicles can be returned");
        }
        transitionTo(VehicleStatus.AVAILABLE);
    }

    public void reportDamage(String description, DamageSeverity severity) {
        if (status != VehicleStatus.RENTED && status != VehicleStatus.AVAILABLE) {
            throw new InvalidStatusTransitionException(
                    "Damage can only be reported for AVAILABLE or RENTED vehicles");
        }
        damageRecords.add(new DamageRecord(description, severity));
        VehicleStatus previous = status;
        status = VehicleStatus.DAMAGED;
        registerEvent(new DamageReported(vehicleId, description, severity));
        if (previous != VehicleStatus.DAMAGED) {
            registerEvent(new VehicleStatusChanged(vehicleId, previous, status));
        }
    }

    public void sendToMaintenance() {
        if (status != VehicleStatus.AVAILABLE && status != VehicleStatus.DAMAGED) {
            throw new InvalidStatusTransitionException(
                    "Only AVAILABLE or DAMAGED vehicles can be sent to maintenance");
        }
        VehicleStatus previous = status;
        status = VehicleStatus.MAINTENANCE;
        registerEvent(new MaintenanceScheduled(vehicleId));
        registerEvent(new VehicleStatusChanged(vehicleId, previous, status));
    }

    public void finishMaintenance() {
        if (status != VehicleStatus.MAINTENANCE) {
            throw new InvalidStatusTransitionException("Only vehicles in MAINTENANCE can finish maintenance");
        }
        transitionTo(VehicleStatus.AVAILABLE);
    }

    public void updateStatus(VehicleStatus newStatus) {
        if (newStatus == status) {
            return;
        }
        if (!isAllowedManualTransition(status, newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Transition from " + status + " to " + newStatus + " is not allowed");
        }
        transitionTo(newStatus);
    }

    private boolean isAllowedManualTransition(VehicleStatus from, VehicleStatus to) {
        return switch (from) {
            case AVAILABLE -> to == VehicleStatus.RESERVED || to == VehicleStatus.RENTED
                    || to == VehicleStatus.MAINTENANCE || to == VehicleStatus.DAMAGED;
            case RESERVED -> to == VehicleStatus.AVAILABLE || to == VehicleStatus.RENTED;
            case RENTED -> to == VehicleStatus.AVAILABLE || to == VehicleStatus.DAMAGED;
            case DAMAGED -> to == VehicleStatus.MAINTENANCE || to == VehicleStatus.AVAILABLE;
            case MAINTENANCE -> to == VehicleStatus.AVAILABLE;
        };
    }

    private void transitionTo(VehicleStatus newStatus) {
        VehicleStatus previous = status;
        status = newStatus;
        registerEvent(new VehicleStatusChanged(vehicleId, previous, newStatus));
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

    public VehicleStatus getStatus() {
        return status;
    }

    public List<DamageRecord> getDamageRecords() {
        return Collections.unmodifiableList(damageRecords);
    }
}
