package com.billing.application;

import com.billing.domain.CustomerId;
import com.billing.domain.Invoice;
import com.billing.domain.RentalCost;
import com.billing.domain.RentalId;
import com.billing.domain.Money;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.ICostCalculatedEventPublisher;
import com.billing.ports.out.IDamageFeeStore;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
import com.billing.ports.out.IRentalSnapshotPort;
import com.billing.ports.out.IVehicleCategoryResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOrPrepareInvoiceUseCase — returning existing invoice or preparing a new one")
class GetOrPrepareInvoiceUseCaseTest {

    @Mock
    private IInvoiceRepository invoiceRepository;

    @Mock
    private IRentalSnapshotPort rentalSnapshotPort;

    @Mock
    private IVehicleCategoryResolver vehicleCategoryResolver;

    @Mock
    private IDomainEventPublisher domainEventPublisher;

    @Mock
    private ICostCalculatedEventPublisher costCalculatedEventPublisher;

    @Mock
    private IDamageFeeStore damageFeeStore;

    private GetOrPrepareInvoiceUseCase useCase;

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        CalculateCostUseCase calculateCostUseCase = new CalculateCostUseCase(
                invoiceRepository, domainEventPublisher,
                costCalculatedEventPublisher, damageFeeStore);
        GenerateInvoiceUseCase generateInvoiceUseCase = new GenerateInvoiceUseCase(
                invoiceRepository, domainEventPublisher);

        useCase = new GetOrPrepareInvoiceUseCase(
                invoiceRepository, rentalSnapshotPort, vehicleCategoryResolver,
                calculateCostUseCase, generateInvoiceUseCase);

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
    @DisplayName("Should return existing invoice when found in repository")
    void shouldReturnExistingInvoiceWhenFound() {
        // given
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.of(invoice));

        // when
        Optional<Invoice> result = useCase.handle(rentalId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(invoice);
    }

    @Test
    @DisplayName("Should return empty optional when invoice not found and no rental snapshot exists")
    void shouldReturnEmptyOptionalWhenInvoiceNotFound() {
        // given
        when(invoiceRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());
        when(rentalSnapshotPort.findById(rentalId)).thenReturn(Optional.empty());

        // when
        Optional<Invoice> result = useCase.handle(rentalId);

        // then
        assertThat(result).isEmpty();
    }
}