package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rental aggregate — state machine and invariants")
class RentalAggregateTest {

    private static final Currency   PLN         = Currency.getInstance("PLN");
    private static final RentalId   RENTAL_ID   = RentalId.of(UUID.randomUUID());
    private static final VehicleId  VEHICLE_ID  = VehicleId.of(UUID.randomUUID());
    private static final CustomerId CUSTOMER_ID = CustomerId.of(UUID.randomUUID());
    private static final DateRange  PERIOD      = DateRange.of(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5)
    );
    private static final Money      FINAL_COST  = Money.of(new BigDecimal("350.00"), PLN);

    // -------------------------------------------------------------------------
    // Existing tests — preserved without modification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should confirm reservation and emit event")
    void shouldConfirmReservationAndEmitEvent() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();

        assertThat(rental.status()).isEqualTo(RentalStatus.CONFIRMED);
        assertThat(rental.domainEvents())
                .anyMatch(e -> e instanceof ReservationCreated);
    }

    @Test
    @DisplayName("should activate rental when customer is eligible")
    void shouldActivateRentalWhenCustomerIsEligible() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();

        assertThat(rental.status()).isEqualTo(RentalStatus.ACTIVE);
    }

    @Test
    @DisplayName("should reject activation when rental is not in confirmable state")
    void shouldRejectActivationWhenCustomerIsNotEligible() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();

        assertThatThrownBy(rental::activate)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should cancel reserved rental")
    void shouldCancelReservedRental() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.cancel();

        assertThat(rental.status()).isEqualTo(RentalStatus.CANCELLED);
    }

    @Test
    @DisplayName("should reject cancellation when rental is active")
    void shouldRejectCancellationWhenRentalIsActive() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();

        assertThatThrownBy(rental::cancel)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should complete active rental with final cost")
    void shouldCompleteActiveRentalWithFinalCost() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();
        rental.complete(FINAL_COST);

        assertThat(rental.status()).isEqualTo(RentalStatus.COMPLETED);
    }

    @Test
    @DisplayName("should reject complete when rental is not active")
    void shouldRejectCompleteWhenRentalIsNotActive() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();

        assertThatThrownBy(() -> rental.complete(FINAL_COST))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should mark active rental as overdue")
    void shouldMarkActiveRentalAsOverdue() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();
        rental.markOverdue();

        assertThat(rental.status()).isEqualTo(RentalStatus.OVERDUE);
    }

    @Test
    @DisplayName("should reject markOverdue when rental is not active")
    void shouldRejectMarkOverdueWhenRentalIsNotActive() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();

        assertThatThrownBy(rental::markOverdue)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // -------------------------------------------------------------------------
    // New tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create rental via factory with RESERVED or DRAFT status and emit ReservationCreated")
    void shouldCreateRentalWithInitialReservedOrDraftStateAccordingToFactory() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);

        assertThat(rental.status()).isIn(RentalStatus.RESERVED, RentalStatus.DRAFT);
        assertThat(rental.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(ReservationCreated.class);
    }

    @Test
    @DisplayName("should reconstitute rental from persistence without emitting domain events")
    void shouldReconstituteRentalWithoutEmittingDomainEvents() {
        Rental rental = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.CONFIRMED, null);

        assertThat(rental.status()).isEqualTo(RentalStatus.CONFIRMED);
        assertThat(rental.domainEvents()).isEmpty();
    }

    @Test
    @DisplayName("should not allow double confirmation")
    void shouldNotAllowDoubleConfirmation() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();

        assertThatThrownBy(rental::confirm)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should not allow second activation")
    void shouldNotAllowSecondActivation() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();

        assertThatThrownBy(rental::activate)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should not allow completion after completion")
    void shouldNotAllowCompletionAfterCompletion() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();
        rental.complete(FINAL_COST);

        assertThatThrownBy(() -> rental.complete(FINAL_COST))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should not allow cancellation after completion")
    void shouldNotAllowCancellationAfterCompletion() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();
        rental.complete(FINAL_COST);

        assertThatThrownBy(rental::cancel)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should not allow markOverdue when rental is cancelled")
    void shouldNotAllowMarkOverdueWhenRentalIsCancelled() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.cancel();

        assertThatThrownBy(rental::markOverdue)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("should keep period and identifiers intact after all state transitions")
    void shouldKeepPeriodAndIdentifiersIntactAfterStateTransitions() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();
        rental.complete(FINAL_COST);

        assertThat(rental.id()).isEqualTo(RENTAL_ID);
        assertThat(rental.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(rental.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(rental.period()).isEqualTo(PERIOD);
    }

    @Test
    @DisplayName("should store final cost after completion")
    void shouldStoreFinalCostAfterCompletion() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();
        rental.complete(FINAL_COST);

        assertThat(rental.finalCost()).isEqualTo(FINAL_COST);
    }

    @Test
    @DisplayName("should expose domain events in emission order")
    void shouldExposeDomainEventsInEmissionOrder() {
        Rental rental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        rental.confirm();
        rental.activate();
        rental.complete(FINAL_COST);

        List<DomainEvent> events = rental.domainEvents();

        assertThat(events).isNotEmpty();
        assertThat(events.get(0)).isInstanceOf(ReservationCreated.class);
    }
}