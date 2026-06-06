package com.billing.adapters;

import com.billing.adapters.in.web.BillingRestController;
import com.billing.application.GetOrPrepareInvoiceUseCase;
import com.billing.application.ProcessPaymentUseCase;
import com.billing.domain.CustomerId;
import com.billing.domain.InvalidInvoiceStateException;
import com.billing.domain.Invoice;
import com.billing.domain.InvoiceNotFoundException;
import com.billing.domain.Money;
import com.billing.domain.RentalCost;
import com.billing.domain.RentalId;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.IInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingRestController — HTTP contract and response codes")
class BillingRestControllerTest {

    private MockMvc mockMvc;

    @Mock private IInvoiceRepository invoiceRepository;
    @Mock private ProcessPaymentUseCase processPaymentUseCase;
    @Mock private GetOrPrepareInvoiceUseCase getOrPrepareInvoiceUseCase;

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BillingRestController(
                invoiceRepository, processPaymentUseCase, getOrPrepareInvoiceUseCase)).build();
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
    @DisplayName("Should return 200 with invoice when found")
    void shouldReturn200WithInvoiceWhenFound() throws Exception {
        // given
        when(getOrPrepareInvoiceUseCase.handle(rentalId)).thenReturn(Optional.of(issuedInvoice()));

        // when + then
        mockMvc.perform(get("/api/v1/invoices/rental-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalId").value("rental-001"))
                .andExpect(jsonPath("$.status").value("ISSUED"));
    }

    @Test
    @DisplayName("Should return 404 when invoice not found")
    void shouldReturn404WhenInvoiceNotFound() throws Exception {
        // given
        when(getOrPrepareInvoiceUseCase.handle(any())).thenReturn(Optional.empty());

        // when + then
        mockMvc.perform(get("/api/v1/invoices/rental-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 201 when payment processed successfully")
    void shouldReturn201WhenPaymentProcessedSuccessfully() throws Exception {
        // given
        doNothing().when(processPaymentUseCase).handle(any());

        String payload = """
                {"rentalId":"rental-001","amount":45000,"currency":"PLN"}
                """;

        // when + then
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 404 when invoice not found during payment")
    void shouldReturn404WhenInvoiceNotFoundDuringPayment() throws Exception {
        // given
        doThrow(new InvoiceNotFoundException("not found"))
                .when(processPaymentUseCase).handle(any());

        String payload = """
                {"rentalId":"rental-001","amount":45000,"currency":"PLN"}
                """;

        // when + then
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 409 when invoice is in wrong state for payment")
    void shouldReturn409WhenInvoiceInWrongStateForPayment() throws Exception {
        // given
        doThrow(new InvalidInvoiceStateException("wrong state"))
                .when(processPaymentUseCase).handle(any());

        String payload = """
                {"rentalId":"rental-001","amount":45000,"currency":"PLN"}
                """;

        // when + then
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 when payment request is invalid")
    void shouldReturn400WhenPaymentRequestInvalid() throws Exception {
        // given — amount = 0 narusza @Min(1)
        String payload = """
                {"rentalId":"rental-001","amount":0,"currency":"PLN"}
                """;

        // when + then
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}