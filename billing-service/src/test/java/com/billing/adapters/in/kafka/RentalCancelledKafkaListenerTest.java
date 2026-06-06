package com.billing.adapters.in.kafka;

import com.billing.domain.CustomerId;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import com.billing.domain.Money;
import com.billing.domain.RentalCost;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.IInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalCancelledKafkaListener — refund issuance on RentalCancelled event")
class RentalCancelledKafkaListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IInvoiceRepository invoiceRepository;

    private RentalCancelledKafkaListener listener;

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        listener = new RentalCancelledKafkaListener(objectMapper, invoiceRepository);
        rentalId = RentalId.of("rental-001");
        customerId = CustomerId.of("customer-001");
    }

    private Invoice paidInvoice() {
        Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        invoice.calculateCost(RentalCost.of(3, 150L, Money.pln(450)));
        invoice.issue();
        invoice.markPaid();
        invoice.clearDomainEvents();
        return invoice;
    }

    private Invoice issuedInvoice() {
        Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        invoice.calculateCost(RentalCost.of(3, 150L, Money.pln(450)));
        invoice.issue();
        invoice.clearDomainEvents();
        return invoice;
    }

    @Test
    @DisplayName("Should issue refund and save when PAID invoice found for cancelled rental")
    void shouldIssueRefundWhenPaidInvoiceFoundForCancelledRental() {
        // given
        Invoice invoice = paidInvoice();
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(invoice));

        String payload = """
                {
                  "eventType": "RentalCancelled",
                  "rentalId": "rental-001"
                }
                """;

        // when
        listener.onRentalCancelled(payload);

        // then
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvoiceStatus.REFUNDED);
    }

    @Test
    @DisplayName("Should not save when invoice is not PAID")
    void shouldNotSaveWhenInvoiceIsNotPaid() {
        // given
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(invoice));

        String payload = """
                {
                  "eventType": "RentalCancelled",
                  "rentalId": "rental-001"
                }
                """;

        // when
        listener.onRentalCancelled(payload);

        // then
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not save when no invoice found for rental")
    void shouldNotSaveWhenNoInvoiceFound() {
        // given
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());

        String payload = """
                {
                  "eventType": "RentalCancelled",
                  "rentalId": "rental-001"
                }
                """;

        // when
        listener.onRentalCancelled(payload);

        // then
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should ignore events with wrong eventType")
    void shouldIgnoreEventsWithWrongEventType() {
        // given
        String payload = """
                {
                  "eventType": "CarReturned",
                  "rentalId": "rental-001"
                }
                """;

        // when
        listener.onRentalCancelled(payload);

        // then
        verify(invoiceRepository, never()).findByRentalId(any());
    }
}