package com.rental.ports.out;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

import com.rental.domain.Customer;
import com.rental.domain.InvalidStatusTransitionException;
import java.util.List;
@Transactional
public abstract class IRentalRepositoryContractTest {

    protected abstract IRentalRepository getRepositoryInstance();

    @Test
    @DisplayName("Should persist and load a reserved rental aggregate")
    void shouldSaveAndFindRental() {
        IRentalRepository repository = getRepositoryInstance();

        RentalId rentalId = RentalId.of("contract-rental-001");
        Rental rental = Rental.create(
                rentalId,
                VehicleId.of("vehicle-001"),
                CustomerId.of("customer-001"),
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(5)));
        rental.confirm();

        repository.save(rental);

        Rental loaded = repository.findById(rentalId);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getStatus()).isEqualTo(RentalStatus.RESERVED);
        assertThat(loaded.getVehicleId().getValue()).isEqualTo("vehicle-001");
    }
    @Test
    @DisplayName("Should update rental status and persist changes")
    void shouldUpdateRentalStatusAndPersistChanges() {
        IRentalRepository repository = getRepositoryInstance();

        RentalId rentalId = RentalId.of("contract-rental-status-001");
        Rental rental = Rental.create(
                rentalId,
                VehicleId.of("vehicle-001"),
                CustomerId.of("customer-001"),
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(5)));
        rental.confirm();
        repository.save(rental);

        // when
        rental.confirmPayment();
        rental.activate(Customer.eligible(CustomerId.of("customer-001")));
        repository.save(rental);

        // then
        Rental loaded = repository.findById(rentalId);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getStatus()).isEqualTo(RentalStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find rentals by customer ID")
    void shouldFindRentalsByCustomerId() {
        IRentalRepository repository = getRepositoryInstance();

        CustomerId targetCustomer = CustomerId.of("customer-findbyid-001");
        CustomerId otherCustomer  = CustomerId.of("customer-findbyid-002");

        Rental r1 = Rental.create(RentalId.of("contract-cust-r1"), VehicleId.of("v-1"),
                targetCustomer, DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)));
        r1.confirm();
        Rental r2 = Rental.create(RentalId.of("contract-cust-r2"), VehicleId.of("v-2"),
                targetCustomer, DateRange.of(LocalDate.now().plusDays(5), LocalDate.now().plusDays(8)));
        r2.confirm();
        Rental r3 = Rental.create(RentalId.of("contract-cust-r3"), VehicleId.of("v-3"),
                otherCustomer, DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)));
        r3.confirm();

        repository.save(r1);
        repository.save(r2);
        repository.save(r3);

        // when
        List<Rental> result = repository.findByCustomerId(targetCustomer);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.getCustomerId().equals(targetCustomer));
    }

    @Test
    @DisplayName("Should find only ACTIVE rentals")
    void shouldFindActiveRentals() {
        IRentalRepository repository = getRepositoryInstance();

        CustomerId customerId = CustomerId.of("customer-active-001");

        // ACTIVE rental
        Rental active = Rental.create(RentalId.of("contract-active-r1"), VehicleId.of("v-a1"),
                customerId, DateRange.of(LocalDate.now(), LocalDate.now().plusDays(5)));
        active.confirm();
        active.confirmPayment();
        active.activate(Customer.eligible(customerId));

        // RESERVED rental
        Rental reserved = Rental.create(RentalId.of("contract-active-r2"), VehicleId.of("v-a2"),
                customerId, DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)));
        reserved.confirm();

        // CANCELLED rental
        Rental cancelled = Rental.create(RentalId.of("contract-active-r3"), VehicleId.of("v-a3"),
                customerId, DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)));
        cancelled.confirm();
        cancelled.cancel();

        repository.save(active);
        repository.save(reserved);
        repository.save(cancelled);

        // when
        List<Rental> result = repository.findActiveRentals();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(RentalStatus.ACTIVE);
    }
}
