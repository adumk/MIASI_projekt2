package com.fleet.adapters.out.db;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;
import com.fleet.infrastructure.persistence.VehicleDocument;
import com.fleet.infrastructure.persistence.VehicleMongoRepository;
import com.fleet.ports.out.IVehicleRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!local")
public class MongoVehicleRepositoryAdapter implements IVehicleRepository {

    private final VehicleMongoRepository mongoRepository;

    public MongoVehicleRepositoryAdapter(VehicleMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public void save(Vehicle vehicle) {
        mongoRepository.save(VehicleDocument.fromDomain(vehicle));
    }

    @Override
    public Vehicle findById(VehicleId vehicleId) {
        return mongoRepository.findById(vehicleId.getValue())
                .map(VehicleDocument::toDomain)
                .orElse(null);
    }

    @Override
    public List<Vehicle> findAll() {
        return mongoRepository.findAll().stream()
                .map(VehicleDocument::toDomain)
                .toList();
    }

    @Override
    public List<Vehicle> search(VehicleStatus status, VehicleCategory category) {
        return findAll().stream()
                .filter(vehicle -> status == null || vehicle.getStatus() == status)
                .filter(vehicle -> category == null || vehicle.getCategory() == category)
                .toList();
    }

    @Override
    public void delete(VehicleId vehicleId) {
        mongoRepository.deleteById(vehicleId.getValue());
    }
}
