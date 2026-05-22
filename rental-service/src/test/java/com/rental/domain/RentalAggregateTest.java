package com.rental.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rental aggregate — state machine and domain event emission")
class RentalAggregateTest {

    private static final LocalDate TODAY      = LocalDate.now();
    private static final LocalDate IN_7_DAYS  = TODAY.plusDays(7);
    private static final LocalDate YESTERDAY  = TODAY.minusDays(1);
    private static final LocalDate WEEK_AGO   = TODAY.minusDays(7);

    private RentalId    rentalId;
    private VehicleId   vehicleId;
    private CustomerId  customerId;
    private DateRange   period;

    @BeforeEach
    void setUp() {
        rentalId   = RentalId.of("rental-001");
        vehicleId  = VehicleId.of("vehicle-001");
        customerId = CustomerId.of("customer-001");
        period     = DateRange.of(TODAY, IN_7_DAYS);
    }

    // =========================================================================
    // Helper — builds a fresh, unconfirmed Rental
    // =========================================================================

    private Rental newRental() {
        return Rental.create(rentalId, vehicleId, customerId, period);
    }

    // =========================================================================
    // confirm()
    // =========================================================================

    @Nested
    @DisplayName("confirm() — initial reservation")
    class Confirm {

        @Test
        @DisplayName("Should transition to RESERVED and emit ReservationCreated event")
        void shouldConfirmReservationAndEmitEvent() {
            // given
            Rental rental = newRental();

            // when
            rental.confirm();

            // then
            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.RESERVED);

            assertThat(rental.getDomainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(ReservationCreated.class);

            ReservationCreated event = (ReservationCreated) rental.getDomainEvents().get(0);
            assertThat(event.getRentalId()).isEqualTo(rentalId);
            assertThat(event.getVehicleId()).isEqualTo(vehicleId);
            assertThat(event.getCustomerId()).isEqualTo(customerId);
            assertThat(event.getPeriod()).isEqualTo(period);
        }
    }

    // =========================================================================
    // activate()
    // =========================================================================

    @Nested
    @DisplayName("activate() — vehicle handover")
    class Activate {

        @Test
        @DisplayName("Should transition to ACTIVE and emit CarRented event when customer is eligible")
        void shouldActivateRentalWhenCustomerIsEligible() {
            // given
            Rental rental = newRental();
            rental.confirm();

            Customer eligibleCustomer = Customer.eligible(customerId);

            // when
            rental.activate(eligibleCustomer);

            // then
            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.ACTIVE);

            assertThat(rental.getDomainEvents())
                    .filteredOn(e -> e instanceof CarRented)
                    .hasSize(1);

            CarRented event = rental.getDomainEvents().stream()
                    .filter(e -> e instanceof CarRented)
                    .map(e -> (CarRented) e)
                    .findFirst()
                    .orElseThrow();

            assertThat(event.getActualStartDate()).isEqualTo(TODAY);
        }

        @Test
        @DisplayName("Should throw CustomerNotEligibleException and keep RESERVED status when customer is blocked")
        void shouldRejectActivationWhenCustomerIsNotEligible() {
            // given
            Rental rental = newRental();
            rental.confirm();

            Customer blockedCustomer = Customer.blocked(customerId);

            // when + then
            assertThatThrownBy(() -> rental.activate(blockedCustomer))
                    .isInstanceOf(CustomerNotEligibleException.class);

            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.RESERVED);
        }
    }

    // =========================================================================
    // cancel()
    // =========================================================================

    @Nested
    @DisplayName("cancel() — reservation cancellation")
    class Cancel {

        @Test
        @DisplayName("Should transition to CANCELLED and emit RentalCancelled event when status is RESERVED")
        void shouldCancelReservedRental() {
            // given
            Rental rental = newRental();
            rental.confirm();

            // when
            rental.cancel();

            // then
            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.CANCELLED);

            assertThat(rental.getDomainEvents())
                    .filteredOn(e -> e instanceof RentalCancelled)
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should throw InvalidStatusTransitionException when cancelling an ACTIVE rental")
        void shouldRejectCancellationWhenRentalIsActive() {
            // given
            Rental rental = newRental();
            rental.confirm();
            rental.activate(Customer.eligible(customerId));

            // when + then
            assertThatThrownBy(() -> rental.cancel())
                    .isInstanceOf(InvalidStatusTransitionException.class);

            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.ACTIVE);
        }
    }

    // =========================================================================
    // complete()
    // =========================================================================

    @Nested
    @DisplayName("complete() — vehicle return")
    class Complete {

        @Test
        @DisplayName("Should transition to COMPLETED and emit CarReturned event with final cost")
        void shouldCompleteActiveRentalWithFinalCost() {
            // given
            Rental rental = newRental();
            rental.confirm();
            rental.activate(Customer.eligible(customerId));

            Money finalCost = Money.of(350, "PLN");

            // when
            rental.complete(finalCost);

            // then
            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.COMPLETED);

            assertThat(rental.getDomainEvents())
                    .filteredOn(e -> e instanceof CarReturned)
                    .hasSize(1);

            CarReturned event = rental.getDomainEvents().stream()
                    .filter(e -> e instanceof CarReturned)
                    .map(e -> (CarReturned) e)
                    .findFirst()
                    .orElseThrow();

            assertThat(event.getFinalCost()).isEqualTo(finalCost);
        }

        @Test
        @DisplayName("Should throw InvalidStatusTransitionException when completing a non-ACTIVE rental")
        void shouldRejectCompleteWhenRentalIsNotActive() {
            // given
            Rental rental = newRental();
            rental.confirm();

            Money finalCost = Money.of(350, "PLN");

            // when + then
            assertThatThrownBy(() -> rental.complete(finalCost))
                    .isInstanceOf(InvalidStatusTransitionException.class);

            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.RESERVED);
        }
    }

    // =========================================================================
    // markOverdue()
    // =========================================================================

    @Nested
    @DisplayName("markOverdue() — overdue detection")
    class MarkOverdue {

        @Test
        @DisplayName("Should transition to OVERDUE when rental is ACTIVE and end date has passed")
        void shouldMarkActiveRentalAsOverdue() {
            // given — rental with a period already in the past
            DateRange expiredPeriod = DateRange.ofHistorical(WEEK_AGO, YESTERDAY);

            Rental rental = Rental.create(rentalId, vehicleId, customerId, expiredPeriod);
            rental.confirm();
            rental.activate(Customer.eligible(customerId));

            // when
            rental.markOverdue();

            // then
            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.OVERDUE);
        }

        @Test
        @DisplayName("Should throw InvalidStatusTransitionException when marking non-ACTIVE rental as overdue")
        void shouldRejectMarkOverdueWhenRentalIsNotActive() {
            // given
            Rental rental = newRental();
            rental.confirm();

            // when + then
            assertThatThrownBy(() -> rental.markOverdue())
                    .isInstanceOf(InvalidStatusTransitionException.class);

            assertThat(rental.getStatus())
                    .isEqualTo(RentalStatus.RESERVED);
        }
    }
}