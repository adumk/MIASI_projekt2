package com.rental.application;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.ReservationCreated;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleId;
import com.rental.domain.VehicleNotAvailableException;
import com.rental.domain.VehicleStatus;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IRentalRepository;
import com.rental.ports.out.IVehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateReservationUseCase — orchestration and side-effect verification")
class CreateReservationUseCaseTest {

    @Mock private IRentalRepository    rentalRepository;
    @Mock private IVehicleRepository   vehicleRepository;
    @Mock private IEventPublisher      eventPublisher;

    @InjectMocks private CreateReservationUseCase sut;

    private static final VehicleId   VEHICLE_ID   = VehicleId.of(UUID.randomUUID());
    private static final CustomerId  CUSTOMER_ID  = CustomerId.of(UUID.randomUUID());
    private static final DateRange   PERIOD       = DateRange.of(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(7)
    );

    // -------------------------------------------------------------------------
    // Existing tests — preserved, identifiers corrected to UUID-based model
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should save rental and publish ReservationCreated event when vehicle is available")
    void shouldSuccessfullyCreateReservation() {
        Vehicle availableVehicle = Vehicle.withStatus(VEHICLE_ID, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);

        CreateReservationCommand command = new CreateReservationCommand(CUSTOMER_ID, VEHICLE_ID, PERIOD);

        sut.handle(command);

        verify(rentalRepository, times(1)).save(any(Rental.class));

        ArgumentCaptor<ReservationCreated> captor = ArgumentCaptor.forClass(ReservationCreated.class);
        verify(eventPublisher, times(1)).publish(captor.capture());

        ReservationCreated published = captor.getValue();
        assertThat(published.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(published.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(published.period()).isEqualTo(PERIOD);
    }

    @Test
    @DisplayName("should throw VehicleNotAvailableException when vehicle status is not AVAILABLE")
    void shouldRejectReservationWhenVehicleIsUnavailable() {
        Vehicle unavailableVehicle = Vehicle.withStatus(VEHICLE_ID, VehicleStatus.RENTED);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(unavailableVehicle);

        CreateReservationCommand command = new CreateReservationCommand(CUSTOMER_ID, VEHICLE_ID, PERIOD);

        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(VehicleNotAvailableException.class);

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    // -------------------------------------------------------------------------
    // New tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should persist rental and publish ReservationCreated exactly once")
    void shouldPersistRentalAndPublishReservationCreatedExactlyOnce() {
        Vehicle availableVehicle = Vehicle.withStatus(VEHICLE_ID, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);

        CreateReservationCommand command = new CreateReservationCommand(CUSTOMER_ID, VEHICLE_ID, PERIOD);
        sut.handle(command);

        verify(rentalRepository, times(1)).save(any(Rental.class));
        verify(eventPublisher, times(1)).publish(any(ReservationCreated.class));
    }

    @Test
    @DisplayName("should reject reservation when period is invalid")
    void shouldRejectReservationWhenPeriodIsInvalid() {
        CreateReservationCommand command = new CreateReservationCommand(
                CUSTOMER_ID,
                VEHICLE_ID,
                null
        );

        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(IllegalArgumentException.class);

        verify(rentalRepository, never()).save(any(Rental.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should throw when vehicle is not found in repository")
    void shouldRejectReservationWhenVehicleIsMissing() {
        when(vehicleRepository.findById(VEHICLE_ID))
                .thenThrow(new VehicleNotAvailableException(VEHICLE_ID));

        CreateReservationCommand command = new CreateReservationCommand(CUSTOMER_ID, VEHICLE_ID, PERIOD);

        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(VehicleNotAvailableException.class);

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    @DisplayName("should not persist anything when reservation fails")
    void shouldNotPersistAnythingWhenReservationFails() {
        Vehicle unavailableVehicle = Vehicle.withStatus(VEHICLE_ID, VehicleStatus.DAMAGED);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(unavailableVehicle);

        CreateReservationCommand command = new CreateReservationCommand(CUSTOMER_ID, VEHICLE_ID, PERIOD);

        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(VehicleNotAvailableException.class);

        verify(rentalRepository, never()).save(any(Rental.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should pass customer, vehicle and period to aggregate exactly once")
    void shouldPassCustomerVehicleAndPeriodToTheAggregateExactlyOnce() {
        Vehicle availableVehicle = Vehicle.withStatus(VEHICLE_ID, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);

        CreateReservationCommand command = new CreateReservationCommand(CUSTOMER_ID, VEHICLE_ID, PERIOD);
        sut.handle(command);

        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(captor.capture());

        Rental saved = captor.getValue();
        assertThat(saved.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(saved.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(saved.period()).isEqualTo(PERIOD);
    }
}