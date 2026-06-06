package com.fleet.application;

import com.fleet.domain.DamageReported;
import com.fleet.domain.DamageSeverity;
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
@DisplayName("ReportDamageUseCase — reporting damage to vehicles")
class ReportDamageUseCaseTest {

    @Mock
    private IVehicleRepository vehicleRepository;

    @Mock
    private IEventPublisher eventPublisher;

    private ReportDamageUseCase useCase;

    private VehicleId vehicleId;

    @BeforeEach
    void setUp() {
        useCase = new ReportDamageUseCase(vehicleRepository, eventPublisher);
        vehicleId = VehicleId.of("vehicle-001");
    }

    private Vehicle rentedVehicle() {
        Vehicle vehicle = Vehicle.create(vehicleId, "WA12345", "Toyota", "Corolla", 2022, VehicleCategory.STANDARD);
        vehicle.rent();
        vehicle.clearDomainEvents();
        return vehicle;
    }

    @Test
    @DisplayName("Should report damage, set status to DAMAGED and publish DamageReported event")
    void shouldReportDamageSetStatusToDamagedAndPublishEvent() {
        // given
        Vehicle vehicle = rentedVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(vehicle);

        // when
        useCase.handle(new ReportDamageCommand(vehicleId, "Scratch on door", DamageSeverity.MINOR));

        // then
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.DAMAGED);
        assertThat(captor.getValue().getDamageRecords()).hasSize(1);

        verify(eventPublisher, atLeastOnce()).publish(argThat(e -> e instanceof DamageReported));
    }

    @Test
    @DisplayName("Should throw VehicleNotFoundException and never save when vehicle does not exist")
    void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {
        // given
        when(vehicleRepository.findById(vehicleId)).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> useCase.handle(
                new ReportDamageCommand(vehicleId, "Scratch", DamageSeverity.MINOR)))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(vehicleRepository, never()).save(any());
    }
}