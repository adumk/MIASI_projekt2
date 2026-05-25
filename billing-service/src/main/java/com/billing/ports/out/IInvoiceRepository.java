package com.billing.ports.out;

import com.billing.domain.Invoice;
import com.billing.domain.RentalId;

import java.util.Optional;

public interface IInvoiceRepository {

    void save(Invoice invoice);

    Optional<Invoice> findByRentalId(RentalId rentalId);
}
