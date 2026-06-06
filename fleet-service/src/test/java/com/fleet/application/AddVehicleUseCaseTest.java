package com.fleet.application;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleAdded;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
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
@DisplayName("AddVehicleUseCase — adding vehicles and publishing events")
class AddVehicleUseCaseTest {

    @Mock
    private IVehicleRepository vehicleRepository;

    @Mock
    private IEventPublisher eventPublisher;

    private AddVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddVehicleUseCase(vehicleRepository, eventPublisher);
    }

    @Test
    @DisplayName("Should create vehicle, save it and publish VehicleAdded event")
    void shouldCreateVehicleSaveAndPublishVehicleAddedEvent() {
        // given
        AddVehicleCommand command = new AddVehicleCommand(
                VehicleId.of("vehicle-001"),
                "WA12345",
                "Toyota",
                "Corolla",
                2022,
                VehicleCategory.STANDARD
        );

        // when
        Vehicle result = useCase.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);

        verify(eventPublisher).publish(argThat(e -> e instanceof VehicleAdded));
    }
}