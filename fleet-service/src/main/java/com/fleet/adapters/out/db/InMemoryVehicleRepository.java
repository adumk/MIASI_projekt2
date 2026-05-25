package com.fleet.adapters.out.db;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;
import com.fleet.ports.out.IVehicleRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("local")
@Primary
public class InMemoryVehicleRepository implements IVehicleRepository {

    private final Map<String, Vehicle> store = new ConcurrentHashMap<>();

    @Override
    public void save(Vehicle vehicle) {
        store.put(vehicle.getVehicleId().getValue(), vehicle);
    }

    @Override
    public Vehicle findById(VehicleId vehicleId) {
        return store.get(vehicleId.getValue());
    }

    @Override
    public List<Vehicle> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<Vehicle> search(VehicleStatus status, VehicleCategory category) {
        return findAll().stream()
                .filter(v -> status == null || v.getStatus() == status)
                .filter(v -> category == null || v.getCategory() == category)
                .toList();
    }

    @Override
    public void delete(VehicleId vehicleId) {
        store.remove(vehicleId.getValue());
    }
}
