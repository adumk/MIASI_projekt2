package com.rental.ports.out;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalNotFoundException;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Abstract contract test for all implementations of {@link IRentalRepository}.
 *
 * Every concrete adapter (Postgres, in-memory, etc.) must extend this class
 * and provide its own implementation of {@link #repository()}. The full
 * contract will then be verified against that adapter automatically.
 */
@DisplayName("IRentalRepository — port contract")
public abstract class IRentalRepositoryContractTest {

    private static final Currency PLN = Currency.getInstance("PLN");

    /**
     * Subclasses must return a fully initialised, clean instance of the
     * repository under test. Each test method receives a fresh state via
     * the setup mechanism of the concrete subclass.
     */
    protected abstract IRentalRepository repository();

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Rental buildReservedRental(RentalId rentalId, VehicleId vehicleId, CustomerId customerId) {
        DateRange period = DateRange.of(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(7)
        );
        Rental rental = Rental.create(rentalId, vehicleId, customerId, period);
        rental.confirm();
        return rental;
    }

    // -------------------------------------------------------------------------
    // Contract tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should save and reload rental aggregate preserving all business fields")
    void shouldSaveAndReloadRentalAggregate() {
        RentalId   rentalId   = RentalId.of(UUID.randomUUID());
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        Rental original = buildReservedRental(rentalId, vehicleId, customerId);
        repository().save(original);

        Rental reloaded = repository().findById(rentalId);

        assertThat(reloaded.id()).isEqualTo(rentalId);
        assertThat(reloaded.vehicleId()).isEqualTo(vehicleId);
        assertThat(reloaded.customerId()).isEqualTo(customerId);
        assertThat(reloaded.status()).isEqualTo(RentalStatus.CONFIRMED);
        assertThat(reloaded.period()).isEqualTo(original.period());
    }

    @Test
    @DisplayName("should overwrite existing rental on subsequent save")
    void shouldOverwriteExistingRentalOnSave() {
        RentalId   rentalId   = RentalId.of(UUID.randomUUID());
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        Rental rental = buildReservedRental(rentalId, vehicleId, customerId);
        repository().save(rental);

        rental.activate();
        repository().save(rental);

        Rental reloaded = repository().findById(rentalId);
        assertThat(reloaded.status()).isEqualTo(RentalStatus.ACTIVE);
    }

    @Test
    @DisplayName("should preserve all rental state fields across a full round-trip")
    void shouldPreserveRentalStateAcrossRoundTrip() {
        RentalId   rentalId   = RentalId.of(UUID.randomUUID());
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        Rental rental = buildReservedRental(rentalId, vehicleId, customerId);
        rental.activate();
        rental.complete(com.rental.domain.Money.of(new BigDecimal("420.00"), PLN));
        repository().save(rental);

        Rental reloaded = repository().findById(rentalId);

        assertThat(reloaded.id()).isEqualTo(rentalId);
        assertThat(reloaded.vehicleId()).isEqualTo(vehicleId);
        assertThat(reloaded.customerId()).isEqualTo(customerId);
        assertThat(reloaded.status()).isEqualTo(RentalStatus.COMPLETED);
        assertThat(reloaded.period()).isEqualTo(rental.period());
        assertThat(reloaded.finalCost()).isEqualTo(rental.finalCost());
    }

    @Test
    @DisplayName("should reload rental without carrying new domain events — reconstitution must be event-silent")
    void shouldNotLoseDomainEventsSemanticsAfterReload() {
        RentalId   rentalId   = RentalId.of(UUID.randomUUID());
        VehicleId  vehicleId  = VehicleId.of(UUID.randomUUID());
        CustomerId customerId = CustomerId.of(UUID.randomUUID());

        Rental rental = buildReservedRental(rentalId, vehicleId, customerId);
        repository().save(rental);

        Rental reloaded = repository().findById(rentalId);

        assertThat(reloaded.domainEvents())
                .as("Reloaded aggregate must not carry any new domain events")
                .isEmpty();
    }

    @Test
    @DisplayName("should throw RentalNotFoundException when rental id is unknown")
    void shouldHandleLoadingUnknownRentalAccordingToRepositoryContract() {
        RentalId unknownId = RentalId.of(UUID.randomUUID());

        assertThatThrownBy(() -> repository().findById(unknownId))
                .isInstanceOf(RentalNotFoundException.class);
    }
}