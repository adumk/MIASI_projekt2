package com.fleet.ports.out;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class IVehicleRepositoryContractTest {

    protected abstract IVehicleRepository getRepositoryInstance();

    private Vehicle availableVehicle(String id, VehicleCategory category) {
        return Vehicle.create(VehicleId.of(id), "WA" + id, "Toyota", "Corolla", 2022, category);
    }

    @Test
    @DisplayName("Should save and find vehicle by ID")
    void shouldSaveAndFindVehicleById() {
        IVehicleRepository repository = getRepositoryInstance();

        Vehicle vehicle = availableVehicle("contract-v-001", VehicleCategory.STANDARD);
        repository.save(vehicle);

        Vehicle found = repository.findById(VehicleId.of("contract-v-001"));

        assertThat(found).isNotNull();
        assertThat(found.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(found.getCategory()).isEqualTo(VehicleCategory.STANDARD);
    }

    @Test
    @DisplayName("Should search vehicles by AVAILABLE status")
    void shouldSearchVehiclesByStatus() {
        IVehicleRepository repository = getRepositoryInstance();

        Vehicle v1 = availableVehicle("contract-v-s1", VehicleCategory.STANDARD);
        Vehicle v2 = availableVehicle("contract-v-s2", VehicleCategory.ECONOMY);
        Vehicle v3 = availableVehicle("contract-v-s3", VehicleCategory.PREMIUM);
        v3.rent();

        repository.save(v1);
        repository.save(v2);
        repository.save(v3);

        List<Vehicle> result = repository.search(VehicleStatus.AVAILABLE, null);

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).allMatch(v -> v.getStatus() == VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should search vehicles by SUV category")
    void shouldSearchVehiclesByCategory() {
        IVehicleRepository repository = getRepositoryInstance();

        Vehicle suv = availableVehicle("contract-v-c1", VehicleCategory.SUV);
        Vehicle std1 = availableVehicle("contract-v-c2", VehicleCategory.STANDARD);
        Vehicle std2 = availableVehicle("contract-v-c3", VehicleCategory.STANDARD);

        repository.save(suv);
        repository.save(std1);
        repository.save(std2);

        List<Vehicle> result = repository.search(null, VehicleCategory.SUV);

        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result).allMatch(v -> v.getCategory() == VehicleCategory.SUV);
    }

    @Test
    @DisplayName("Should return null when vehicle not found")
    void shouldReturnNullWhenVehicleNotFound() {
        IVehicleRepository repository = getRepositoryInstance();

        Vehicle found = repository.findById(VehicleId.of("nonexistent-vehicle"));

        assertThat(found).isNull();
    }
}