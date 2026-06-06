package com.fleet.application;

import com.fleet.domain.DamageSeverity;
import com.fleet.domain.InvalidStatusTransitionException;
import com.fleet.domain.MaintenanceScheduled;
import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleNotFoundException;
import com.fleet.domain.VehicleStatus;
import com.fleet.ports.out.IEventPublisher;
import com.fleet.ports.out.IVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleMaintenanceUseCase — sending vehicles to maintenance")
class ScheduleMaintenanceUseCaseTest {

    @Mock
    private IVehicleRepository vehicleRepository;

    @Mock
    private IEventPublisher eventPublisher;

    private ScheduleMaintenanceUseCase useCase;

    private VehicleId vehicleId;

    @BeforeEach
    void setUp() {
        useCase = new ScheduleMaintenanceUseCase(vehicleRepository, eventPublisher);
        vehicleId = VehicleId.of("vehicle-001");
    }

    private Vehicle damagedVehicle() {
        Vehicle vehicle = Vehicle.create(vehicleId, "WA12345", "Toyota", "Corolla", 2022, VehicleCategory.STANDARD);
        vehicle.rent();
        vehicle.reportDamage("Scratch on bumper", DamageSeverity.MINOR);
        vehicle.clearDomainEvents();
        return vehicle;
    }

    @Test
    @DisplayName("Should send DAMAGED vehicle to maintenance and publish MaintenanceScheduled event")
    void shouldSendVehicleToMaintenanceAndPublishEvent() {
        // given
        Vehicle vehicle = damagedVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(vehicle);

        // when
        useCase.handle(new ScheduleMaintenanceCommand(vehicleId));

        // then
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);

        verify(eventPublisher, atLeastOnce()).publish(argThat(e -> e instanceof MaintenanceScheduled));
    }

    @Test
    @DisplayName("Should throw VehicleNotFoundException when vehicle does not exist")
    void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findById(vehicleId)).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new ScheduleMaintenanceCommand(vehicleId)))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when vehicle is RENTED (not valid for maintenance)")
    void shouldThrowWhenVehicleIsNotInValidStateForMaintenance() {
        // given
        Vehicle vehicle = Vehicle.create(vehicleId, "WA12345", "Toyota", "Corolla", 2022, VehicleCategory.STANDARD);
        vehicle.rent();
        vehicle.clearDomainEvents();
        when(vehicleRepository.findById(vehicleId)).thenReturn(vehicle);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new ScheduleMaintenanceCommand(vehicleId)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(vehicleRepository, never()).save(any());
    }
}