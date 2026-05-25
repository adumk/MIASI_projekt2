package com.rental.application;

import com.rental.domain.Rental;
import com.rental.ports.out.ICustomerVerificationPort;
import com.rental.ports.out.IRentalRepository;

public class ConfirmReservationUseCase {

    private final IRentalRepository rentalRepository;
    private final ICustomerVerificationPort customerVerificationPort;

    public ConfirmReservationUseCase(
            IRentalRepository rentalRepository,
            ICustomerVerificationPort customerVerificationPort) {
        this.rentalRepository = rentalRepository;
        this.customerVerificationPort = customerVerificationPort;
    }

    public void handle(ConfirmReservationCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId());
        customerVerificationPort.findEligibleCustomer(rental.getCustomerId());
        rental.confirmPayment();
        rentalRepository.save(rental);
    }
}
