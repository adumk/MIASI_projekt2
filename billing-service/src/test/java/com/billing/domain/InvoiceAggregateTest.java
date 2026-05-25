package com.billing.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Invoice aggregate — cost calculation, issuance and payment")
class InvoiceAggregateTest {

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        rentalId = RentalId.of("rental-001");
        customerId = CustomerId.of("customer-001");
    }

    @Nested
    @DisplayName("calculateCost()")
    class CalculateCost {

        @Test
        @DisplayName("Should transition to COST_CALCULATED and emit CostCalculated event")
        void shouldCalculateCostAndEmitEvent() {
            Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
            RentalCost cost = RentalCost.of(3, 150L, Money.pln(450));

            invoice.calculateCost(cost);

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.COST_CALCULATED);
            assertThat(invoice.getRentalCost()).isEqualTo(cost);
            assertThat(invoice.getDomainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(CostCalculated.class);
        }

        @Test
        @DisplayName("Should reject cost calculation when invoice is not DRAFT")
        void shouldRejectCostCalculationWhenNotDraft() {
            Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.STANDARD);
            invoice.calculateCost(RentalCost.of(1, 100L, Money.pln(100)));

            assertThatThrownBy(() -> invoice.calculateCost(RentalCost.of(2, 100L, Money.pln(200))))
                    .isInstanceOf(InvalidInvoiceStateException.class);
        }
    }

    @Nested
    @DisplayName("issue()")
    class Issue {

        @Test
        @DisplayName("Should transition to ISSUED and emit InvoiceGenerated event")
        void shouldIssueInvoice() {
            Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.PREMIUM);
            invoice.calculateCost(RentalCost.of(2, 250L, Money.pln(500)));

            invoice.issue();

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
            assertThat(invoice.getDomainEvents())
                    .filteredOn(e -> e instanceof InvoiceGenerated)
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("markPaid()")
    class MarkPaid {

        @Test
        @DisplayName("Should transition to PAID when invoice is ISSUED")
        void shouldMarkInvoiceAsPaid() {
            Invoice invoice = Invoice.createDraft(rentalId, customerId, VehicleCategory.ECONOMY);
            invoice.calculateCost(RentalCost.of(1, 100L, Money.pln(100)));
            invoice.issue();

            invoice.markPaid();

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        }
    }
}
