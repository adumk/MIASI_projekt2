package com.billing.application;

import com.billing.domain.CustomerId;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceStatus;
import com.billing.domain.RentalId;
import com.billing.ports.out.IInvoiceRepository;
import com.billing.ports.out.IRentalSnapshotPort;
import com.billing.ports.out.IVehicleCategoryResolver;

import java.util.Optional;

public class GetOrPrepareInvoiceUseCase {

    private final IInvoiceRepository invoiceRepository;
    private final IRentalSnapshotPort rentalSnapshotPort;
    private final IVehicleCategoryResolver vehicleCategoryResolver;
    private final CalculateCostUseCase calculateCostUseCase;
    private final GenerateInvoiceUseCase generateInvoiceUseCase;

    public GetOrPrepareInvoiceUseCase(
            IInvoiceRepository invoiceRepository,
            IRentalSnapshotPort rentalSnapshotPort,
            IVehicleCategoryResolver vehicleCategoryResolver,
            CalculateCostUseCase calculateCostUseCase,
            GenerateInvoiceUseCase generateInvoiceUseCase) {
        this.invoiceRepository = invoiceRepository;
        this.rentalSnapshotPort = rentalSnapshotPort;
        this.vehicleCategoryResolver = vehicleCategoryResolver;
        this.calculateCostUseCase = calculateCostUseCase;
        this.generateInvoiceUseCase = generateInvoiceUseCase;
    }

    public Optional<Invoice> handle(RentalId rentalId) {
        Optional<Invoice> existing = invoiceRepository.findByRentalId(rentalId);
        if (existing.isPresent()) {
            return Optional.of(ensureIssued(rentalId, existing.get()));
        }

        return rentalSnapshotPort.findById(rentalId).flatMap(snapshot -> {
            if (!"COMPLETED".equals(snapshot.status())) {
                return Optional.empty();
            }
            calculateCostUseCase.handle(new CalculateCostCommand(
                    rentalId,
                    CustomerId.of(snapshot.customerId()),
                    snapshot.vehicleId(),
                    vehicleCategoryResolver.resolve(snapshot.vehicleId()),
                    snapshot.periodStart(),
                    snapshot.periodEnd()));

            generateInvoiceUseCase.handle(new GenerateInvoiceCommand(rentalId));
            return invoiceRepository.findByRentalId(rentalId).map(inv -> ensureIssued(rentalId, inv));
        });
    }

    private Invoice ensureIssued(RentalId rentalId, Invoice invoice) {
        if (invoice.getStatus() == InvoiceStatus.COST_CALCULATED) {
            generateInvoiceUseCase.handle(new GenerateInvoiceCommand(rentalId));
            return invoiceRepository.findByRentalId(rentalId).orElse(invoice);
        }
        return invoice;
    }
}
