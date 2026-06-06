package com.billing.application;

import com.billing.domain.CustomerId;
import com.billing.domain.InvalidInvoiceStateException;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceGenerated;
import com.billing.domain.InvoiceNotFoundException;
import com.billing.domain.InvoiceStatus;
import com.billing.domain.Money;
import com.billing.domain.RentalCost;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
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
@DisplayName("GenerateInvoiceUseCase — issuing invoices and publishing events")
class GenerateInvoiceUseCaseTest {

    @Mock
    private IInvoiceRepository invoiceRepository;

    @Mock
    private IDomainEventPublisher domainEventPublisher;

    private GenerateInvoiceUseCase useCase;

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        useCase = new GenerateInvoiceUseCase(invoiceRepository, domainEventPublisher);
        rentalId = RentalId.of("rental-001");
        customerId = CustomerId.of("customer-001");
    }

    private Invoice costCalculatedInvoice() {
        Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        invoice.calculateCost(RentalCost.of(3, 150L, Money.pln(450)));
        invoice.clearDomainEvents();
        return invoice;
    }

    @Test
    @DisplayName("Should issue invoice and publish InvoiceGenerated event")
    void shouldIssueInvoiceAndPublishEvent() {
        // given
        Invoice invoice = costCalculatedInvoice();
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(invoice));

        // when
        useCase.handle(new GenerateInvoiceCommand(rentalId));

        // then
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        verify(domainEventPublisher).publish(argThat(e -> e instanceof InvoiceGenerated));
    }

    @Test
    @DisplayName("Should throw InvoiceNotFoundException when no invoice exists for rental")
    void shouldThrowInvoiceNotFoundExceptionWhenNoInvoice() {
        // given
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> useCase.handle(new GenerateInvoiceCommand(rentalId)))
                .isInstanceOf(InvoiceNotFoundException.class);

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidInvoiceStateException when invoice is not in COST_CALCULATED state")
    void shouldThrowWhenInvoiceNotInCostCalculatedState() {
        // given — invoice still in DRAFT (no calculateCost called)
        Invoice draftInvoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(draftInvoice));

        // when + then
        assertThatThrownBy(() -> useCase.handle(new GenerateInvoiceCommand(rentalId)))
                .isInstanceOf(InvalidInvoiceStateException.class);

        assertThat(draftInvoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }
}