package com.billing.ports.out;

import com.billing.domain.RentalId;

import java.time.LocalDate;
import java.util.Optional;

public interface IRentalBillingSessionStore {

    void startSession(RentalId rentalId, LocalDate actualStartDate);

    Optional<LocalDate> findStartDate(RentalId rentalId);
}
