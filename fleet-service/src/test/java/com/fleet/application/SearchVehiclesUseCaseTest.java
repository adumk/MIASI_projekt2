package com.fleet.application;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;
import com.fleet.ports.out.IVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchVehiclesUseCase — filtering vehicles by status and category")
class SearchVehiclesUseCaseTest {

    @Mock
    private IVehicleRepository vehicleRepository;

    private SearchVehiclesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchVehiclesUseCase(vehicleRepository);
    }

    private Vehicle availableVehicle(String id, VehicleCategory category) {
        return Vehicle.create(VehicleId.of(id), "WA" + id, "Toyota", "Corolla", 2022, category);
    }

    @Test
    @DisplayName("Should return vehicles matching AVAILABLE status filter")
    void shouldReturnVehiclesMatchingStatusFilter() {
        // given
        List<Vehicle> available = List.of(
                availableVehicle("v-1", VehicleCategory.STANDARD),
                availableVehicle("v-2", VehicleCategory.ECONOMY)
        );
        when(vehicleRepository.search(VehicleStatus.AVAILABLE, null)).thenReturn(available);

        // when
        List<Vehicle> result = useCase.handle(new SearchVehiclesQuery(VehicleStatus.AVAILABLE, null));

        // then
        assertThat(result).hasSize(2);
        verify(vehicleRepository).search(VehicleStatus.AVAILABLE, null);
    }

    @Test
    @DisplayName("Should return vehicles matching SUV category filter")
    void shouldReturnVehiclesMatchingCategoryFilter() {
        // given
        List<Vehicle> suvs = List.of(availableVehicle("v-3", VehicleCategory.SUV));
        when(vehicleRepository.search(null, VehicleCategory.SUV)).thenReturn(suvs);

        // when
        List<Vehicle> result = useCase.handle(new SearchVehiclesQuery(null, VehicleCategory.SUV));

        // then
        assertThat(result).hasSize(1);
        verify(vehicleRepository).search(null, VehicleCategory.SUV);
    }

    @Test
    @DisplayName("Should return empty list when no vehicles match")
    void shouldReturnEmptyListWhenNoVehiclesMatch() {
        // given
        when(vehicleRepository.search(VehicleStatus.RENTED, VehicleCategory.PREMIUM))
                .thenReturn(List.of());

        // when
        List<Vehicle> result = useCase.handle(
                new SearchVehiclesQuery(VehicleStatus.RENTED, VehicleCategory.PREMIUM));

        // then
        assertThat(result).isEmpty();
    }
}