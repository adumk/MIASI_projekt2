package com.billing.application;

import com.billing.domain.CostCalculated;
import com.billing.domain.CustomerId;
import com.billing.domain.DomainEvent;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import com.billing.domain.Money;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.ICostCalculatedEventPublisher;
import com.billing.ports.out.IDamageFeeStore;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculateCostUseCase — invoice creation and cost calculation")
class CalculateCostUseCaseTest {

    @Mock
    private IInvoiceRepository invoiceRepository;

    @Mock
    private IDomainEventPublisher domainEventPublisher;

    @Mock
    private ICostCalculatedEventPublisher costCalculatedEventPublisher;

    @Mock
    private IDamageFeeStore damageFeeStore;

    private CalculateCostUseCase useCase;

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        useCase = new CalculateCostUseCase(
                invoiceRepository, domainEventPublisher,
                costCalculatedEventPublisher, damageFeeStore);
        rentalId = RentalId.of("rental-001");
        customerId = CustomerId.of("customer-001");
    }

    private CalculateCostCommand economyCommand(int days) {
        LocalDate start = LocalDate.of(2026, 6, 1);
        return new CalculateCostCommand(
                rentalId, customerId, "vehicle-001",
                VehicleCategory.ECONOMY, start, start.plusDays(days));
    }

    @Test
    @DisplayName("Should create draft invoice, calculate cost and publish events when none exists")
    void shouldCreateDraftInvoiceAndCalculateCostWhenNoneExists() {
        // given
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());
        when(damageFeeStore.consumePendingFee("vehicle-001")).thenReturn(0L);

        // when
        useCase.handle(economyCommand(3));

        // then
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvoiceStatus.COST_CALCULATED);
        assertThat(captor.getValue().getRentalCost().getTotal()).isEqualTo(Money.pln(300));
        verify(costCalculatedEventPublisher).publish(eq(rentalId), eq(customerId), eq(Money.pln(300)));
        verify(domainEventPublisher).publish(any(CostCalculated.class));
    }

    @Test
    @DisplayName("Should reuse existing draft invoice when one already exists")
    void shouldReuseExistingDraftInvoiceWhenAlreadyExists() {
        // given
        Invoice existing = Invoice.createDraft(rentalId, customerId, VehicleCategory.ECONOMY);
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(existing));
        when(damageFeeStore.consumePendingFee("vehicle-001")).thenReturn(0L);

        // when
        useCase.handle(economyCommand(3));

        // then
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existing);
        assertThat(captor.getValue().getStatus()).isEqualTo(InvoiceStatus.COST_CALCULATED);
    }

    @Test
    @DisplayName("Should add damage fee to the calculated cost")
    void shouldAddDamageFeeToCalculatedCost() {
        // given
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());
        when(damageFeeStore.consumePendingFee("vehicle-001")).thenReturn(5000L);

        // when
        useCase.handle(economyCommand(2));

        // then
        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        // ECONOMY 100 * 2 days + 5000 damage = 5200
        assertThat(captor.getValue().getRentalCost().getTotal()).isEqualTo(Money.pln(5200));
        verify(damageFeeStore).consumePendingFee("vehicle-001");
    }

    @Test
    @DisplayName("Should publish CostCalculated domain event")
    void shouldPublishCostCalculatedDomainEvent() {
        // given
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());
        when(damageFeeStore.consumePendingFee("vehicle-001")).thenReturn(0L);

        // when
        useCase.handle(economyCommand(1));

        // then
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(CostCalculated.class);
    }
}