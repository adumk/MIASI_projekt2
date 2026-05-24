package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain events — immutability and payload contract")
class DomainEventTest {

    private static final Currency    PLN         = Currency.getInstance("PLN");
    private static final RentalId    RENTAL_ID   = RentalId.of(UUID.randomUUID());
    private static final VehicleId   VEHICLE_ID  = VehicleId.of(UUID.randomUUID());
    private static final CustomerId  CUSTOMER_ID = CustomerId.of(UUID.randomUUID());
    private static final DateRange   PERIOD      = DateRange.of(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5)
    );
    private static final Money       FINAL_COST  = Money.of(new BigDecimal("350.00"), PLN);

    // -------------------------------------------------------------------------
    // ReservationCreated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create ReservationCreated with full payload")
    void shouldCreateReservationCreatedWithFullPayload() {
        ReservationCreated event = ReservationCreated.of(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);

        assertThat(event.rentalId()).isEqualTo(RENTAL_ID);
        assertThat(event.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(event.period()).isEqualTo(PERIOD);
    }

    // -------------------------------------------------------------------------
    // CarRented
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create CarRented with full payload")
    void shouldCreateCarRentedWithFullPayload() {
        CarRented event = CarRented.of(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID);

        assertThat(event.rentalId()).isEqualTo(RENTAL_ID);
        assertThat(event.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);
    }

    // -------------------------------------------------------------------------
    // CarReturned
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create CarReturned with full payload")
    void shouldCreateCarReturnedWithFullPayload() {
        LocalDate returnDate = LocalDate.now().plusDays(5);
        CarReturned event = CarReturned.of(RENTAL_ID, VEHICLE_ID, returnDate, FINAL_COST);

        assertThat(event.rentalId()).isEqualTo(RENTAL_ID);
        assertThat(event.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(event.returnDate()).isEqualTo(returnDate);
        assertThat(event.finalCost()).isEqualTo(FINAL_COST);
    }

    // -------------------------------------------------------------------------
    // RentalCancelled
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create RentalCancelled with full payload")
    void shouldCreateRentalCancelledWithFullPayload() {
        RentalCancelled event = RentalCancelled.of(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID);

        assertThat(event.rentalId()).isEqualTo(RENTAL_ID);
        assertThat(event.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(event.customerId()).isEqualTo(CUSTOMER_ID);
    }

    // -------------------------------------------------------------------------
    // VehicleStatusChanged
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create VehicleStatusChanged with full payload")
    void shouldCreateVehicleStatusChangedWithFullPayload() {
        VehicleStatusChanged event = VehicleStatusChanged.of(VEHICLE_ID, VehicleStatus.RENTED);

        assertThat(event.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(event.newStatus()).isEqualTo(VehicleStatus.RENTED);
    }

    // -------------------------------------------------------------------------
    // Common metadata — occurredAt and eventId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should expose non-null occurredAt and unique eventId on every event")
    void shouldExposeOccurredAtAndEventId() {
        ReservationCreated first  = ReservationCreated.of(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        ReservationCreated second = ReservationCreated.of(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);

        assertThat(first.occurredAt()).isNotNull();
        assertThat(first.eventId()).isNotNull();
        assertThat(first.eventId()).isNotEqualTo(second.eventId());
    }

    // -------------------------------------------------------------------------
    // Immutability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should have no public setters — events must be immutable")
    void shouldBeImmutable() {
        Class<?>[] eventClasses = {
                ReservationCreated.class,
                CarRented.class,
                CarReturned.class,
                RentalCancelled.class,
                VehicleStatusChanged.class
        };

        for (Class<?> eventClass : eventClasses) {
            long setterCount = java.util.Arrays.stream(eventClass.getMethods())
                    .filter(m -> m.getName().startsWith("set"))
                    .count();

            assertThat(setterCount)
                    .as("Event class %s must have no public setters", eventClass.getSimpleName())
                    .isZero();
        }
    }

    // -------------------------------------------------------------------------
    // Value comparability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should not be value-equal across two separately created instances — each event is a unique fact")
    void shouldBeValueComparableWhereApplicable() {
        ReservationCreated first  = ReservationCreated.of(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        ReservationCreated second = ReservationCreated.of(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);

        // Two separate occurrences of the same business fact are not the same event —
        // they differ by eventId and occurredAt.
        assertThat(first).isNotEqualTo(second);
    }
}