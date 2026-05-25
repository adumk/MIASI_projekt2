package com.rental.application;

import com.rental.domain.RentalId;

public record CancelReservationCommand(RentalId rentalId) {
}
