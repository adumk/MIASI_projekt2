package com.rental.ports.out;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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
}
