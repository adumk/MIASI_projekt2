package com.rental.application;

import com.rental.domain.Customer;
import com.rental.domain.DomainEvent;
import com.rental.domain.Rental;
import com.rental.ports.out.ICustomerVerificationPort;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IRentalRepository;

public class RentVehicleUseCase {

    private final IRentalRepository rentalRepository;
    private final ICustomerVerificationPort customerVerificationPort;
    private final IEventPublisher eventPublisher;

    public RentVehicleUseCase(
            IRentalRepository rentalRepository,
            ICustomerVerificationPort customerVerificationPort,
            IEventPublisher eventPublisher) {
        this.rentalRepository = rentalRepository;
        this.customerVerificationPort = customerVerificationPort;
        this.eventPublisher = eventPublisher;
    }

    public void handle(RentVehicleCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId());
        Customer customer = customerVerificationPort.findEligibleCustomer(rental.getCustomerId());

        rental.activate(customer);
        rentalRepository.save(rental);
        publishEvents(rental);
    }

    private void publishEvents(Rental rental) {
        for (DomainEvent event : rental.getDomainEvents()) {
            eventPublisher.publish(event);
        }
        rental.clearDomainEvents();
    }
}
