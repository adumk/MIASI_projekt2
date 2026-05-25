package com.rental.adapters.in.web;

import com.rental.domain.Rental;
import com.rental.domain.RentalStatus;

import java.time.LocalDate;

public record RentalResponse(
        String rentalId,
        String vehicleId,
        String customerId,
        LocalDate periodStart,
        LocalDate periodEnd,
        RentalStatus status,
        boolean paymentConfirmed) {

    public static RentalResponse from(Rental rental) {
        return new RentalResponse(
                rental.getRentalId().getValue(),
                rental.getVehicleId().getValue(),
                rental.getCustomerId().getValue(),
                rental.getPeriod().getStart(),
                rental.getPeriod().getEnd(),
                rental.getStatus(),
                rental.isPaymentConfirmed());
    }
}
