package com.billing.adapters.out.db;

import com.billing.domain.InvoiceId;
import com.billing.domain.Money;
import com.billing.domain.Payment;
import com.billing.domain.PaymentId;
import com.billing.domain.PaymentStatus;
import com.billing.domain.RentalId;
import com.billing.infrastructure.persistence.PaymentJpaEntity;
import com.billing.infrastructure.persistence.PaymentJpaRepository;
import com.billing.ports.out.IPaymentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresPaymentRepositoryAdapter implements IPaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PostgresPaymentRepositoryAdapter(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity(
                payment.getPaymentId().getValue(),
                payment.getInvoiceId().getValue(),
                payment.getRentalId().getValue(),
                payment.getAmount().toMinorUnits(),
                payment.getAmount().getCurrency(),
                payment.getStatus().name());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Payment> findByRentalId(RentalId rentalId) {
        return jpaRepository.findByRentalId(rentalId.getValue())
                .map(entity -> Payment.reconstitute(
                        PaymentId.of(entity.getPaymentId()),
                        InvoiceId.of(entity.getInvoiceId()),
                        RentalId.of(entity.getRentalId()),
                        Money.of(entity.getAmount(), entity.getCurrency()),
                        PaymentStatus.valueOf(entity.getStatus())));
    }
}
