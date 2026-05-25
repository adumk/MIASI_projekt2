package com.rental.application;

import com.rental.domain.Rental;
import com.rental.ports.out.IRentalRepository;

import java.util.List;

public class GetRentalHistoryUseCase {

    private final IRentalRepository rentalRepository;

    public GetRentalHistoryUseCase(IRentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public List<Rental> handle(GetRentalHistoryQuery query) {
        return rentalRepository.findByCustomerId(query.customerId());
    }
}
