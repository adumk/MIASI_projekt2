package com.rental.application;

import com.rental.domain.CarReturned;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.DomainEvent;
import com.rental.domain.InvalidStatusTransitionException;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalNotFoundException;
import com.rental.domain.RentalStatus;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleId;
import com.rental.domain.VehicleNotAvailableException;
import com.rental.domain.VehicleStatus;
import com.rental.domain.VehicleStatusChanged;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnVehicleUseCase — state transition, persistence and event publication")
class ReturnVehicleUseCaseTest {

    @Mock private IRentalRepository  rentalRepository;
    @Mock private IVehicleRepository vehicleRepository;
    @Mock private IEventPublisher    eventPublisher;

    @InjectMocks private ReturnVehicleUseCase sut;

    private static final RentalId   RENTAL_ID         = RentalId.of(UUID.randomUUID());
    private static final VehicleId  VEHICLE_ID        = VehicleId.of(UUID.randomUUID());
    private static final CustomerId CUSTOMER_ID       = CustomerId.of(UUID.randomUUID());
    private static final DateRange  PERIOD            = DateRange.of(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(7)
    );
    private static final LocalDate  ACTUAL_RETURN_ON  = LocalDate.now().plusDays(8);

    @Test
    @DisplayName("should complete rental, persist both aggregates and publish CarReturned and VehicleStatusChanged")
    void shouldSuccessfullyReturnVehicle() {
        Rental activeRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        activeRental.confirm();
        activeRental.activate();

        Vehicle rentedVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.RENTED);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(activeRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(rentedVehicle);

        sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON));

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());
        assertThat(rentalCaptor.getValue().status()).isEqualTo(RentalStatus.COMPLETED);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository, times(1)).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().status()).isEqualTo(VehicleStatus.AVAILABLE);

        verify(eventPublisher, times(1)).publish(any(CarReturned.class));
        verify(eventPublisher, times(1)).publish(any(VehicleStatusChanged.class));
    }


    @Test
    @DisplayName("should throw when rental does not exist")
    void shouldRejectWhenRentalDoesNotExist() {
        when(rentalRepository.findById(RENTAL_ID))
                .thenThrow(new RentalNotFoundException(RENTAL_ID));

        assertThatThrownBy(() -> sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON)))
                .isInstanceOf(RentalNotFoundException.class);

        verify(rentalRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should throw when rental is not ACTIVE")
    void shouldRejectWhenRentalIsNotActive() {
        Rental completedRental = Rental.reconstitute(
                RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.COMPLETED, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(completedRental);

        assertThatThrownBy(() -> sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(rentalRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should throw when vehicle does not exist")
    void shouldRejectWhenVehicleDoesNotExist() {
        Rental activeRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        activeRental.confirm();
        activeRental.activate();

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(activeRental);
        when(vehicleRepository.findById(VEHICLE_ID))
                .thenThrow(new VehicleNotAvailableException(VEHICLE_ID));

        assertThatThrownBy(() -> sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON)))
                .isInstanceOf(VehicleNotAvailableException.class);

        verify(rentalRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should persist rental and vehicle exactly once each")
    void shouldPersistRentalAndVehicleExactlyOnce() {
        Rental activeRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        activeRental.confirm();
        activeRental.activate();

        Vehicle rentedVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.RENTED);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(activeRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(rentedVehicle);

        sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON));

        verify(rentalRepository, times(1)).save(any(Rental.class));
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("should publish CarReturned and VehicleStatusChanged events")
    void shouldPublishCarReturnedAndVehicleStatusChangedEvents() {
        Rental activeRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        activeRental.confirm();
        activeRental.activate();

        Vehicle rentedVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.RENTED);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(activeRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(rentedVehicle);

        sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(captor.capture());

        List<DomainEvent> published = captor.getAllValues();
        assertThat(published).filteredOn(e -> e instanceof CarReturned).hasSize(1);
        assertThat(published).filteredOn(e -> e instanceof VehicleStatusChanged).hasSize(1);
    }

    @Test
    @DisplayName("should not publish events on failure")
    void shouldNotPublishEventsOnFailure() {
        when(rentalRepository.findById(RENTAL_ID))
                .thenThrow(new RentalNotFoundException(RENTAL_ID));

        assertThatThrownBy(() -> sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON)))
                .isInstanceOf(RentalNotFoundException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should carry actual return date in CarReturned event payload")
    void shouldStoreActualReturnDateInRentalCompletion() {
        Rental activeRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        activeRental.confirm();
        activeRental.activate();

        Vehicle rentedVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.RENTED);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(activeRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(rentedVehicle);

        sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON));

        ArgumentCaptor<CarReturned> captor = ArgumentCaptor.forClass(CarReturned.class);
        verify(eventPublisher, times(1)).publish(captor.capture());

        assertThat(captor.getValue().returnDate()).isEqualTo(ACTUAL_RETURN_ON);
    }

    @Test
    @DisplayName("should complete rental before returning vehicle — rental status must be COMPLETED on save")
    void shouldCompleteRentalBeforeReturningVehicleStatus() {
        Rental activeRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        activeRental.confirm();
        activeRental.activate();

        Vehicle rentedVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.RENTED);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(activeRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(rentedVehicle);

        sut.handle(new ReturnVehicleCommand(RENTAL_ID, ACTUAL_RETURN_ON));

        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(RentalStatus.COMPLETED);
    }
}