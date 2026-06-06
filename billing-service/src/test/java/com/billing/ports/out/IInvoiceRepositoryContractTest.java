package com.billing.ports.out;

import com.billing.domain.CustomerId;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import com.billing.domain.Money;
import com.billing.domain.RentalCost;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class IInvoiceRepositoryContractTest {

    protected abstract IInvoiceRepository getRepositoryInstance();

    @Test
    @DisplayName("Should save and find invoice by rentalId")
    void shouldSaveAndFindInvoiceByRentalId() {
        IInvoiceRepository repository = getRepositoryInstance();

        RentalId rentalId = RentalId.of("contract-invoice-001");
        CustomerId customerId = CustomerId.of("customer-001");

        Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        repository.save(invoice);

        Optional<Invoice> found = repository.findByRentalId(rentalId);

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(found.get().getRentalId().getValue()).isEqualTo("contract-invoice-001");
    }

    @Test
    @DisplayName("Should persist COST_CALCULATED status and rental cost")
    void shouldPersistCostCalculatedStatus() {
        IInvoiceRepository repository = getRepositoryInstance();

        RentalId rentalId = RentalId.of("contract-invoice-002");
        CustomerId customerId = CustomerId.of("customer-001");

        Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
        invoice.calculateCost(RentalCost.of(3, 150L, Money.pln(450)));
        repository.save(invoice);

        Optional<Invoice> found = repository.findByRentalId(rentalId);

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(InvoiceStatus.COST_CALCULATED);
        assertThat(found.get().getRentalCost().getTotal()).isEqualTo(Money.pln(450));
    }

    @Test
    @DisplayName("Should return empty Optional when invoice not found")
    void shouldReturnEmptyOptionalWhenInvoiceNotFound() {
        IInvoiceRepository repository = getRepositoryInstance();

        Optional<Invoice> found = repository.findByRentalId(RentalId.of("nonexistent-invoice"));

        assertThat(found).isEmpty();
    }
}