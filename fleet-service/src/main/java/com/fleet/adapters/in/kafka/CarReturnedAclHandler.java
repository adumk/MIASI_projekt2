package com.fleet.adapters.in.kafka;

import com.fleet.domain.DomainEvent;
import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleNotFoundException;
import com.fleet.ports.out.IEventPublisher;
import com.fleet.ports.out.IVehicleRepository;
import com.rental.events.CarReturnedEvent;
import org.springframework.stereotype.Component;

@Component
public class CarReturnedAclHandler {

    private final IVehicleRepository vehicleRepository;
    private final IEventPublisher eventPublisher;

    public CarReturnedAclHandler(IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        this.vehicleRepository = vehicleRepository;
        this.eventPublisher = eventPublisher;
    }

    public void handle(CarReturnedEvent event) {
        Vehicle vehicle = vehicleRepository.findById(VehicleId.of(event.getVehicleId()));
        if (vehicle == null) {
            throw new VehicleNotFoundException("Vehicle not found: " + event.getVehicleId());
        }
        vehicle.returnVehicle();
        vehicleRepository.save(vehicle);
        publishEvents(vehicle);
    }

    private void publishEvents(Vehicle vehicle) {
        for (DomainEvent domainEvent : vehicle.getDomainEvents()) {
            eventPublisher.publish(domainEvent);
        }
        vehicle.clearDomainEvents();
    }
}
