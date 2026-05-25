package com.rental.application;

import com.rental.domain.DomainEvent;
import com.rental.domain.Rental;
import com.rental.ports.out.IBillingQuotePort;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IFleetVehicleInfoPort;
import com.rental.ports.out.IRentalRepository;

public class ReturnVehicleUseCase {

    private final IRentalRepository rentalRepository;
    private final IEventPublisher eventPublisher;
    private final IFleetVehicleInfoPort fleetVehicleInfoPort;
    private final IBillingQuotePort billingQuotePort;

    public ReturnVehicleUseCase(
            IRentalRepository rentalRepository,
            IEventPublisher eventPublisher,
            IFleetVehicleInfoPort fleetVehicleInfoPort,
            IBillingQuotePort billingQuotePort) {
        this.rentalRepository = rentalRepository;
        this.eventPublisher = eventPublisher;
        this.fleetVehicleInfoPort = fleetVehicleInfoPort;
        this.billingQuotePort = billingQuotePort;
    }

    public void handle(ReturnVehicleCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId());
        String category = fleetVehicleInfoPort.resolveCategory(rental.getVehicleId());
        var finalCost = billingQuotePort.quoteRentalCost(
                category, rental.getPeriod().getStart(), command.actualReturnDate());

        rental.complete(
                finalCost,
                command.actualReturnDate(),
                command.returnMileage(),
                command.inspectionNotes());
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
