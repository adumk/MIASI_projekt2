package com.rental.application;

import com.rental.domain.DomainEvent;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.VehicleNotAvailableException;
import com.rental.ports.out.IFleetAvailabilityPort;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IRentalRepository;

public class CreateReservationUseCase {

    private final IRentalRepository rentalRepository;
    private final IFleetAvailabilityPort fleetAvailabilityPort;
    private final IEventPublisher eventPublisher;

    public CreateReservationUseCase(
            IRentalRepository rentalRepository,
            IFleetAvailabilityPort fleetAvailabilityPort,
            IEventPublisher eventPublisher) {
        this.rentalRepository = rentalRepository;
        this.fleetAvailabilityPort = fleetAvailabilityPort;
        this.eventPublisher = eventPublisher;
    }

    public Rental handle(CreateReservationCommand command) {
        if (!fleetAvailabilityPort.isAvailable(command.vehicleId(), command.period())) {
            throw new VehicleNotAvailableException("Vehicle is not available for reservation");
        }
        if (rentalRepository.hasOverlappingBooking(command.vehicleId(), command.period(), null)) {
            throw new VehicleNotAvailableException("Vehicle is already booked for the selected period");
        }

        Rental rental = Rental.create(
                RentalId.generate(),
                command.vehicleId(),
                command.customerId(),
                command.period());
        rental.confirm();

        rentalRepository.save(rental);
        publishEvents(rental);
        return rental;
    }

    private void publishEvents(Rental rental) {
        for (DomainEvent event : rental.getDomainEvents()) {
            eventPublisher.publish(event);
        }
        rental.clearDomainEvents();
    }
}
