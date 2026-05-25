package com.billing.application;

import com.billing.domain.CustomerId;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;

import java.time.LocalDate;

public record CalculateCostCommand(
        RentalId rentalId,
        CustomerId customerId,
        String vehicleId,
        VehicleCategory vehicleCategory,
        LocalDate rentalStartDate,
        LocalDate returnDate) {
}
