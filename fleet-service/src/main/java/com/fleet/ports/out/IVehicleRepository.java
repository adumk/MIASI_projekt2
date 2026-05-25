package com.fleet.ports.out;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;

import java.util.List;

public interface IVehicleRepository {

    void save(Vehicle vehicle);

    Vehicle findById(VehicleId vehicleId);

    List<Vehicle> findAll();

    List<Vehicle> search(VehicleStatus status, VehicleCategory category);

    void delete(VehicleId vehicleId);
}
