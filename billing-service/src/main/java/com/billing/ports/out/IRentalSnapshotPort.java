package com.billing.ports.out;

import com.billing.domain.RentalId;

import java.time.LocalDate;
import java.util.Optional;

public interface IRentalSnapshotPort {

    Optional<RentalSnapshot> findById(RentalId rentalId);

    record RentalSnapshot(
            String rentalId,
            String vehicleId,
            String customerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String status) {}
}
