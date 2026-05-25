package com.rental.application;

import com.rental.domain.RentalId;

public record ConfirmReservationCommand(RentalId rentalId) {
}
