package com.billing.ports.out;

import com.billing.domain.Payment;
import com.billing.domain.RentalId;

import java.util.Optional;

public interface IPaymentRepository {

    void save(Payment payment);

    Optional<Payment> findByRentalId(RentalId rentalId);
}
