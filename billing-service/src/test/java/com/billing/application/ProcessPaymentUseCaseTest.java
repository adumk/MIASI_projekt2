package com.billing.application;

import com.billing.domain.CustomerId;
import com.billing.domain.InvalidInvoiceStateException;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceNotFoundException;
import com.billing.domain.InvoiceStatus;
import com.billing.domain.Money;
import com.billing.domain.Payment;
import com.billing.domain.PaymentConfirmed;
import com.billing.domain.PaymentStatus;
import com.billing.domain.RentalCost;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
import com.billing.ports.out.IPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentUseCase — processing payments and updating invoice status")
class ProcessPaymentUseCaseTest {

    @Mock
    private IInvoiceRepository invoiceRepository;

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private IDomainEventPublisher domainEventPublisher;

    private ProcessPaymentUseCase useCase;

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentUseCase(invoiceRepository, paymentRepository, domainEventPublisher);
        rentalId = RentalId.of("rental-001");
        customerId = CustomerId.of("customer-001");
    }

    private Invoice issuedInvoice() {
        Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        invoice.calculateCost(RentalCost.of(3, 150L, Money.pln(450)));
        invoice.issue();
        invoice.clearDomainEvents();
        return invoice;
    }

    @Test
    @DisplayName("Should create payment, mark invoice as PAID and publish PaymentConfirmed event")
    void shouldCreatePaymentMarkInvoicePaidAndPublishEvents() {
        // given
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(invoice));

        // when
        useCase.handle(new ProcessPaymentCommand(rentalId, Money.pln(450)));

        // then
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.CONFIRMED);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getStatus()).isEqualTo(InvoiceStatus.PAID);

        verify(domainEventPublisher, atLeastOnce())
                .publish(argThat(e -> e instanceof PaymentConfirmed));
    }

    @Test
    @DisplayName("Should throw InvoiceNotFoundException when no invoice found and never save payment")
    void shouldThrowInvoiceNotFoundExceptionWhenNoInvoice() {
        // given
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> useCase.handle(new ProcessPaymentCommand(rentalId, Money.pln(450))))
                .isInstanceOf(InvoiceNotFoundException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidInvoiceStateException and never save payment when invoice is not ISSUED")
    void shouldThrowWhenInvoiceNotInIssuedState() {
        // given — invoice in DRAFT state (not issued)
        Invoice draftInvoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(draftInvoice));

        // when + then
        assertThatThrownBy(() -> useCase.handle(new ProcessPaymentCommand(rentalId, Money.pln(450))))
                .isInstanceOf(InvalidInvoiceStateException.class);

        verify(paymentRepository, never()).save(any());
    }
}