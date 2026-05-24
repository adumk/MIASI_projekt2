package com.rental.application;

import com.rental.domain.CarRented;
import com.rental.domain.Customer;
import com.rental.domain.CustomerNotEligibleException;
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
import com.rental.ports.out.ICustomerRepository;
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
@DisplayName("RentVehicleUseCase — state transition, persistence and event publication")
class RentVehicleUseCaseTest {

    @Mock private IRentalRepository   rentalRepository;
    @Mock private IVehicleRepository  vehicleRepository;
    @Mock private ICustomerRepository customerRepository;
    @Mock private IEventPublisher     eventPublisher;

    @InjectMocks private RentVehicleUseCase sut;

    private static final RentalId   RENTAL_ID   = RentalId.of(UUID.randomUUID());
    private static final VehicleId  VEHICLE_ID  = VehicleId.of(UUID.randomUUID());
    private static final CustomerId CUSTOMER_ID = CustomerId.of(UUID.randomUUID());
    private static final DateRange  PERIOD      = DateRange.of(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(7)
    );

    @Test
    @DisplayName("should activate rental, persist both aggregates and publish CarRented and VehicleStatusChanged")
    void shouldSuccessfullyRentVehicle() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle availableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.AVAILABLE);
        Customer eligibleCustomer = Customer.eligible(CUSTOMER_ID);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(eligibleCustomer);

        sut.handle(new RentVehicleCommand(RENTAL_ID));

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());
        assertThat(rentalCaptor.getValue().status()).isEqualTo(RentalStatus.ACTIVE);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository, times(1)).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().status()).isEqualTo(VehicleStatus.RENTED);

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());

        List<DomainEvent> published = eventCaptor.getAllValues();
        assertThat(published).filteredOn(e -> e instanceof CarRented).hasSize(1);
        assertThat(published).filteredOn(e -> e instanceof VehicleStatusChanged).hasSize(1);

        VehicleStatusChanged statusChanged = published.stream()
                .filter(e -> e instanceof VehicleStatusChanged)
                .map(e -> (VehicleStatusChanged) e)
                .findFirst().orElseThrow();

        assertThat(statusChanged.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(statusChanged.newStatus()).isEqualTo(VehicleStatus.RENTED);
    }

    @Test
    @DisplayName("should throw when rental does not exist")
    void shouldRejectWhenRentalDoesNotExist() {
        when(rentalRepository.findById(RENTAL_ID))
                .thenThrow(new RentalNotFoundException(RENTAL_ID));

        assertThatThrownBy(() -> sut.handle(new RentVehicleCommand(RENTAL_ID)))
                .isInstanceOf(RentalNotFoundException.class);

        verify(rentalRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should throw when vehicle is not available")
    void shouldRejectWhenVehicleIsNotAvailable() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle unavailableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.RENTED);
        Customer eligibleCustomer = Customer.eligible(CUSTOMER_ID);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(unavailableVehicle);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(eligibleCustomer);

        assertThatThrownBy(() -> sut.handle(new RentVehicleCommand(RENTAL_ID)))
                .isInstanceOf(VehicleNotAvailableException.class);

        verify(rentalRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should throw when customer cannot rent")
    void shouldRejectWhenCustomerCannotRent() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle availableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.AVAILABLE);
        Customer ineligibleCustomer = Customer.ineligible(CUSTOMER_ID);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(ineligibleCustomer);

        assertThatThrownBy(() -> sut.handle(new RentVehicleCommand(RENTAL_ID)))
                .isInstanceOf(CustomerNotEligibleException.class);

        verify(rentalRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should throw when customer repository does not return eligible customer")
    void shouldRejectWhenCustomerRepositoryDoesNotReturnEligibleCustomer() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle availableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.AVAILABLE);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);
        when(customerRepository.findById(CUSTOMER_ID))
                .thenThrow(new RentalNotFoundException(RENTAL_ID));

        assertThatThrownBy(() -> sut.handle(new RentVehicleCommand(RENTAL_ID)))
                .isInstanceOf(RuntimeException.class);

        verify(rentalRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should persist rental and vehicle exactly once each")
    void shouldPersistRentalAndVehicleExactlyOnce() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle availableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.AVAILABLE);
        Customer eligibleCustomer = Customer.eligible(CUSTOMER_ID);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(eligibleCustomer);

        sut.handle(new RentVehicleCommand(RENTAL_ID));

        verify(rentalRepository, times(1)).save(any(Rental.class));
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("should publish CarRented and VehicleStatusChanged events")
    void shouldPublishCarRentedAndVehicleStatusChangedEvents() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle availableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.AVAILABLE);
        Customer eligibleCustomer = Customer.eligible(CUSTOMER_ID);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(eligibleCustomer);

        sut.handle(new RentVehicleCommand(RENTAL_ID));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(captor.capture());

        List<DomainEvent> published = captor.getAllValues();
        assertThat(published).filteredOn(e -> e instanceof CarRented).hasSize(1);
        assertThat(published).filteredOn(e -> e instanceof VehicleStatusChanged).hasSize(1);
    }

    @Test
    @DisplayName("should not publish events on failure")
    void shouldNotPublishEventsOnFailure() {
        when(rentalRepository.findById(RENTAL_ID))
                .thenThrow(new RentalNotFoundException(RENTAL_ID));

        assertThatThrownBy(() -> sut.handle(new RentVehicleCommand(RENTAL_ID)))
                .isInstanceOf(RentalNotFoundException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should load rental, vehicle and customer before any state change")
    void shouldLoadRentalVehicleAndCustomerBeforeStateChange() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle availableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.AVAILABLE);
        Customer eligibleCustomer = Customer.eligible(CUSTOMER_ID);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(eligibleCustomer);

        sut.handle(new RentVehicleCommand(RENTAL_ID));

        verify(rentalRepository, times(1)).findById(RENTAL_ID);
        verify(vehicleRepository, times(1)).findById(VEHICLE_ID);
        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
    }

    @Test
    @DisplayName("should activate rental before persisting — rental status must be ACTIVE on save")
    void shouldActivateRentalBeforeRentingVehicle() {
        Rental reservedRental = Rental.create(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD);
        reservedRental.confirm();

        Vehicle availableVehicle = Vehicle.reconstitute(VEHICLE_ID, VehicleStatus.AVAILABLE);
        Customer eligibleCustomer = Customer.eligible(CUSTOMER_ID);

        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reservedRental);
        when(vehicleRepository.findById(VEHICLE_ID)).thenReturn(availableVehicle);
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(eligibleCustomer);

        sut.handle(new RentVehicleCommand(RENTAL_ID));

        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(RentalStatus.ACTIVE);
    }
}