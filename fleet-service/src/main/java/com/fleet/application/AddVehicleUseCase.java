package com.fleet.application;

import com.fleet.domain.DomainEvent;
import com.fleet.domain.Vehicle;
import com.fleet.ports.out.IEventPublisher;
import com.fleet.ports.out.IVehicleRepository;

public class AddVehicleUseCase {

    private final IVehicleRepository vehicleRepository;
    private final IEventPublisher eventPublisher;

    public AddVehicleUseCase(IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        this.vehicleRepository = vehicleRepository;
        this.eventPublisher = eventPublisher;
    }

    public Vehicle handle(AddVehicleCommand command) {
        Vehicle vehicle = Vehicle.create(
                command.vehicleId(),
                command.licensePlate(),
                command.brand(),
                command.model(),
                command.year(),
                command.category());
        vehicleRepository.save(vehicle);
        publishEvents(vehicle);
        return vehicle;
    }

    private void publishEvents(Vehicle vehicle) {
        for (DomainEvent event : vehicle.getDomainEvents()) {
            eventPublisher.publish(event);
        }
        vehicle.clearDomainEvents();
    }
}
