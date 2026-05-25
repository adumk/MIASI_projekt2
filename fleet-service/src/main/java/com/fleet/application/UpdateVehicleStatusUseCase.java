package com.fleet.application;

import com.fleet.domain.DomainEvent;
import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleNotFoundException;
import com.fleet.ports.out.IEventPublisher;
import com.fleet.ports.out.IVehicleRepository;

public class UpdateVehicleStatusUseCase {

    private final IVehicleRepository vehicleRepository;
    private final IEventPublisher eventPublisher;

    public UpdateVehicleStatusUseCase(IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        this.vehicleRepository = vehicleRepository;
        this.eventPublisher = eventPublisher;
    }

    public void handle(UpdateVehicleStatusCommand command) {
        Vehicle vehicle = requireVehicle(command.vehicleId());
        vehicle.updateStatus(command.status());
        vehicleRepository.save(vehicle);
        publishEvents(vehicle);
    }

    private Vehicle requireVehicle(com.fleet.domain.VehicleId vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) {
            throw new VehicleNotFoundException("Vehicle not found: " + vehicleId.getValue());
        }
        return vehicle;
    }

    private void publishEvents(Vehicle vehicle) {
        for (DomainEvent event : vehicle.getDomainEvents()) {
            eventPublisher.publish(event);
        }
        vehicle.clearDomainEvents();
    }
}
