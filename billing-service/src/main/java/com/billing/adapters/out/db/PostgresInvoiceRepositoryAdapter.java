package com.billing.adapters.out.db;

import com.billing.domain.CustomerId;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceId;
import com.billing.domain.InvoiceStatus;
import com.billing.domain.Money;
import com.billing.domain.RentalCost;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;
import com.billing.infrastructure.persistence.InvoiceJpaEntity;
import com.billing.infrastructure.persistence.InvoiceJpaRepository;
import com.billing.ports.out.IInvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresInvoiceRepositoryAdapter implements IInvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;

    public PostgresInvoiceRepositoryAdapter(InvoiceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Invoice invoice) {
        RentalCost cost = invoice.getRentalCost();
        long amount = cost != null ? cost.getTotal().toMinorUnits() : 0L;
        String currency = cost != null ? cost.getTotal().getCurrency() : "PLN";
        int days = cost != null ? cost.getRentalDays() : 0;

        InvoiceJpaEntity entity = new InvoiceJpaEntity(
                invoice.getInvoiceId().getValue(),
                invoice.getRentalId().getValue(),
                invoice.getCustomerId().getValue(),
                amount,
                currency,
                days,
                invoice.getVehicleCategory().name(),
                invoice.getStatus().name());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Invoice> findByRentalId(RentalId rentalId) {
        return jpaRepository.findByRentalId(rentalId.getValue()).map(this::toDomain);
    }

    private Invoice toDomain(InvoiceJpaEntity entity) {
        RentalCost cost = null;
        if (entity.getRentalDays() > 0 && entity.getAmount() > 0) {
            cost = RentalCost.of(
                    entity.getRentalDays(),
                    entity.getAmount() / entity.getRentalDays(),
                    Money.of(entity.getAmount(), entity.getCurrency()));
        }

        return Invoice.reconstitute(
                InvoiceId.of(entity.getInvoiceId()),
                RentalId.of(entity.getRentalId()),
                CustomerId.of(entity.getCustomerId()),
                VehicleCategory.valueOf(entity.getVehicleCategory()),
                cost,
                InvoiceStatus.valueOf(entity.getStatus()));
    }
}
