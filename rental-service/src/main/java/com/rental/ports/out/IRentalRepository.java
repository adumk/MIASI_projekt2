package com.rental.ports.out;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;

import java.util.List;

public interface IRentalRepository {

    void save(Rental rental);

    Rental findById(RentalId rentalId);

    List<Rental> findByCustomerId(CustomerId customerId);

    List<Rental> findActiveRentals();

    List<Rental> findByStatus(RentalStatus status);

    List<Rental> findAll();

    long countByStatus(RentalStatus status);

    boolean hasOverlappingBooking(VehicleId vehicleId, DateRange period, RentalId excludeRentalId);

    List<String> findBusyVehicleIds(DateRange period);
}
