package com.rental.application;

import com.rental.domain.DomainEvent;
import com.rental.domain.Rental;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IRentalRepository;

public class CancelReservationUseCase {

    private final IRentalRepository rentalRepository;
    private final IEventPublisher eventPublisher;

    public CancelReservationUseCase(IRentalRepository rentalRepository, IEventPublisher eventPublisher) {
        this.rentalRepository = rentalRepository;
        this.eventPublisher = eventPublisher;
    }

    public void handle(CancelReservationCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId());

        rental.cancel();
        rentalRepository.save(rental);

        for (DomainEvent event : rental.getDomainEvents()) {
            eventPublisher.publish(event);
        }
        rental.clearDomainEvents();
    }
}
