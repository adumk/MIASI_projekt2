package com.billing.application;

import com.billing.domain.DomainEvent;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceNotFoundException;
import com.billing.domain.Payment;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
import com.billing.ports.out.IPaymentRepository;

public class ProcessPaymentUseCase {

    private final IInvoiceRepository invoiceRepository;
    private final IPaymentRepository paymentRepository;
    private final IDomainEventPublisher domainEventPublisher;

    public ProcessPaymentUseCase(
            IInvoiceRepository invoiceRepository,
            IPaymentRepository paymentRepository,
            IDomainEventPublisher domainEventPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void handle(ProcessPaymentCommand command) {
        Invoice invoice = invoiceRepository
                .findByRentalId(command.rentalId())
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found for rental: " + command.rentalId().getValue()));

        Payment payment = Payment.create(
                invoice.getInvoiceId(),
                invoice.getRentalId(),
                command.amount());

        payment.confirm();
        invoice.markPaid();

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);

        publishDomainEvents(payment);
        publishDomainEvents(invoice);
    }

    private void publishDomainEvents(Payment payment) {
        for (DomainEvent event : payment.getDomainEvents()) {
            domainEventPublisher.publish(event);
        }
        payment.clearDomainEvents();
    }

    private void publishDomainEvents(Invoice invoice) {
        for (DomainEvent event : invoice.getDomainEvents()) {
            domainEventPublisher.publish(event);
        }
        invoice.clearDomainEvents();
    }
}
