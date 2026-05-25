package com.fleet.adapters.in.web;

import com.fleet.domain.DamageRecord;
import com.fleet.domain.Vehicle;

import java.time.Instant;
import java.util.List;

public record VehicleResponse(
        String vehicleId,
        String licensePlate,
        String brand,
        String model,
        int year,
        String category,
        String status,
        long dailyRateMinorUnits,
        List<DamageRecordResponse> damageRecords) {

    public static VehicleResponse fromDomain(Vehicle vehicle, long dailyRateMinorUnits) {
        return new VehicleResponse(
                vehicle.getVehicleId().getValue(),
                vehicle.getLicensePlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getCategory().name(),
                vehicle.getStatus().name(),
                dailyRateMinorUnits,
                vehicle.getDamageRecords().stream()
                        .map(DamageRecordResponse::fromDomain)
                        .toList());
    }

    public record DamageRecordResponse(String description, String severity, Instant reportedAt) {

        public static DamageRecordResponse fromDomain(DamageRecord record) {
            return new DamageRecordResponse(
                    record.getDescription(),
                    record.getSeverity().name(),
                    record.getReportedAt());
        }
    }
}
