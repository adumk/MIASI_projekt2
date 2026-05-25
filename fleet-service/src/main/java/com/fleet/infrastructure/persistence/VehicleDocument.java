package com.fleet.infrastructure.persistence;

import com.fleet.domain.DamageRecord;
import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "vehicles")
public class VehicleDocument {

    @Id
    private String vehicleId;
    private String licensePlate;
    private String brand;
    private String model;
    private int year;
    private String category;
    private String status;
    private List<DamageRecordDocument> damageRecords = new ArrayList<>();

    public VehicleDocument() {
    }

    public static VehicleDocument fromDomain(Vehicle vehicle) {
        VehicleDocument document = new VehicleDocument();
        document.vehicleId = vehicle.getVehicleId().getValue();
        document.licensePlate = vehicle.getLicensePlate();
        document.brand = vehicle.getBrand();
        document.model = vehicle.getModel();
        document.year = vehicle.getYear();
        document.category = vehicle.getCategory().name();
        document.status = vehicle.getStatus().name();
        document.damageRecords = vehicle.getDamageRecords().stream()
                .map(DamageRecordDocument::fromDomain)
                .toList();
        return document;
    }

    public Vehicle toDomain() {
        List<DamageRecord> records = damageRecords == null
                ? List.of()
                : damageRecords.stream().map(DamageRecordDocument::toDomain).toList();
        return Vehicle.reconstitute(
                VehicleId.of(vehicleId),
                licensePlate,
                brand,
                model,
                year,
                VehicleCategory.valueOf(category),
                VehicleStatus.valueOf(status),
                records);
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<DamageRecordDocument> getDamageRecords() {
        return damageRecords;
    }

    public void setDamageRecords(List<DamageRecordDocument> damageRecords) {
        this.damageRecords = damageRecords;
    }
}
