package com.rental.application;

import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.ports.out.IRentalRepository;

public class CloseSettlementUseCase {

    private final IRentalRepository rentalRepository;

    public CloseSettlementUseCase(IRentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public void handle(RentalId rentalId) {
        Rental rental = rentalRepository.findById(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("Rental not found: " + rentalId.getValue());
        }
        rental.closeSettlement();
        rentalRepository.save(rental);
    }
}
