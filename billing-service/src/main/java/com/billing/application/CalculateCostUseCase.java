package com.billing.application;

import com.billing.domain.DomainEvent;
import com.billing.domain.Invoice;
import com.billing.domain.RentalCost;
import com.billing.domain.TariffCalculator;
import com.billing.ports.out.ICostCalculatedEventPublisher;
import com.billing.ports.out.IDamageFeeStore;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;

public class CalculateCostUseCase {

    private final IInvoiceRepository invoiceRepository;
    private final IDomainEventPublisher domainEventPublisher;
    private final ICostCalculatedEventPublisher costCalculatedEventPublisher;
    private final IDamageFeeStore damageFeeStore;

    public CalculateCostUseCase(
            IInvoiceRepository invoiceRepository,
            IDomainEventPublisher domainEventPublisher,
            ICostCalculatedEventPublisher costCalculatedEventPublisher,
            IDamageFeeStore damageFeeStore) {
        this.invoiceRepository = invoiceRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.costCalculatedEventPublisher = costCalculatedEventPublisher;
        this.damageFeeStore = damageFeeStore;
    }

    public void handle(CalculateCostCommand command) {
        Invoice invoice = invoiceRepository
                .findByRentalId(command.rentalId())
                .orElseGet(() -> Invoice.createDraft(
                        command.rentalId(),
                        command.customerId(),
                        command.vehicleCategory()));

        long damageFee = damageFeeStore.consumePendingFee(command.vehicleId());
        RentalCost cost = TariffCalculator.calculate(
                command.vehicleCategory(),
                command.rentalStartDate(),
                command.returnDate(),
                damageFee);

        invoice.calculateCost(cost);
        invoiceRepository.save(invoice);

        publishDomainEvents(invoice);
        costCalculatedEventPublisher.publish(
                command.rentalId(),
                command.customerId(),
                cost.getTotal());
    }

    private void publishDomainEvents(Invoice invoice) {
        for (DomainEvent event : invoice.getDomainEvents()) {
            domainEventPublisher.publish(event);
        }
        invoice.clearDomainEvents();
    }
}
