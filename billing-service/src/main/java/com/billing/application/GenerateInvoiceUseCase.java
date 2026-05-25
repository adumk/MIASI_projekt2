package com.billing.application;

import com.billing.domain.DomainEvent;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceNotFoundException;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;

public class GenerateInvoiceUseCase {

    private final IInvoiceRepository invoiceRepository;
    private final IDomainEventPublisher domainEventPublisher;

    public GenerateInvoiceUseCase(
            IInvoiceRepository invoiceRepository,
            IDomainEventPublisher domainEventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void handle(GenerateInvoiceCommand command) {
        Invoice invoice = invoiceRepository
                .findByRentalId(command.rentalId())
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found for rental: " + command.rentalId().getValue()));

        invoice.issue();
        invoiceRepository.save(invoice);
        publishDomainEvents(invoice);
    }

    private void publishDomainEvents(Invoice invoice) {
        for (DomainEvent event : invoice.getDomainEvents()) {
            domainEventPublisher.publish(event);
        }
        invoice.clearDomainEvents();
    }
}
