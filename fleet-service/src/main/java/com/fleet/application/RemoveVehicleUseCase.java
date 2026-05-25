package com.fleet.application;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleNotFoundException;
import com.fleet.ports.out.IVehicleRepository;

public class RemoveVehicleUseCase {

    private final IVehicleRepository vehicleRepository;

    public RemoveVehicleUseCase(IVehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public void handle(RemoveVehicleCommand command) {
        Vehicle vehicle = vehicleRepository.findById(command.vehicleId());
        if (vehicle == null) {
            throw new VehicleNotFoundException("Vehicle not found: " + command.vehicleId());
        }
        vehicle.removeFromFleet();
        vehicleRepository.delete(command.vehicleId());
    }
}
