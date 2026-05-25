package com.fleet.application;

import com.fleet.domain.Vehicle;
import com.fleet.ports.out.IVehicleRepository;

import java.util.List;

public class SearchVehiclesUseCase {

    private final IVehicleRepository vehicleRepository;

    public SearchVehiclesUseCase(IVehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> handle(SearchVehiclesQuery query) {
        return vehicleRepository.search(query.status(), query.category());
    }
}
