package com.rental.application;

import com.rental.domain.Rental;
import com.rental.ports.out.IRentalRepository;

import java.time.LocalDate;
import java.util.List;

public class MarkOverdueUseCase {

    private final IRentalRepository rentalRepository;

    public MarkOverdueUseCase(IRentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public int handle() {
        LocalDate today = LocalDate.now();
        int marked = 0;
        for (Rental rental : rentalRepository.findActiveRentals()) {
            if (rental.getPeriod().getEnd().isBefore(today)) {
                rental.markOverdue();
                rentalRepository.save(rental);
                marked++;
            }
        }
        return marked;
    }
}
