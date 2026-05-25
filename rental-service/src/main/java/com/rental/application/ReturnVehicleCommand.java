package com.rental.application;

import com.rental.domain.RentalId;

import java.time.LocalDate;

public record ReturnVehicleCommand(
        RentalId rentalId,
        LocalDate actualReturnDate,
        Integer returnMileage,
        String inspectionNotes) {
}
