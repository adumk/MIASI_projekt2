package com.billing.adapters.in.web;

import com.billing.application.GetOrPrepareInvoiceUseCase;
import com.billing.application.ProcessPaymentCommand;
import com.billing.application.ProcessPaymentUseCase;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceNotFoundException;
import com.billing.domain.Money;
import com.billing.domain.RentalId;
import com.billing.ports.out.IInvoiceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BillingRestController {

    private final IInvoiceRepository invoiceRepository;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final GetOrPrepareInvoiceUseCase getOrPrepareInvoiceUseCase;

    public BillingRestController(
            IInvoiceRepository invoiceRepository,
            ProcessPaymentUseCase processPaymentUseCase,
            GetOrPrepareInvoiceUseCase getOrPrepareInvoiceUseCase) {
        this.invoiceRepository = invoiceRepository;
        this.processPaymentUseCase = processPaymentUseCase;
        this.getOrPrepareInvoiceUseCase = getOrPrepareInvoiceUseCase;
    }

    @GetMapping("/invoices/{rentalId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable("rentalId") String rentalId) {
        return getOrPrepareInvoiceUseCase
                .handle(RentalId.of(rentalId))
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/payments")
    public ResponseEntity<Void> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        try {
            processPaymentUseCase.handle(new ProcessPaymentCommand(
                    RentalId.of(request.rentalId()),
                    Money.of(request.amount(), request.currency())));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (InvoiceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | com.billing.domain.InvalidInvoiceStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        if (invoice.getRentalCost() == null) {
            throw new IllegalStateException("Invoice cost not calculated yet");
        }
        return new InvoiceResponse(
                invoice.getInvoiceId().getValue(),
                invoice.getRentalId().getValue(),
                invoice.getCustomerId().getValue(),
                invoice.getRentalCost().getTotal().toMinorUnits(),
                invoice.getRentalCost().getTotal().getCurrency(),
                invoice.getRentalCost().getRentalDays(),
                invoice.getVehicleCategory().name(),
                invoice.getStatus().name());
    }
}
