package com.rental.application;

import com.rental.domain.CarRented;
import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.DomainEvent;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleId;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentVehicleUseCase — state transition, persistence and event publication")
class RentVehicleUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    @Mock
    private IVehicleRepository vehicleRepository;

    @Mock
    private ICustomerRepository customerRepository;

    @Mock
    private IEventPublisher eventPublisher;

    @InjectMocks
    private RentVehicleUseCase sut;

    private static final LocalDate TODAY     = LocalDate.now();
    private static final LocalDate IN_7_DAYS = TODAY.plusDays(7);

    // =========================================================================
    // shouldSuccessfullyRentVehicle
    // =========================================================================

    @Test
    @DisplayName("Should activate rental, persist both aggregates and publish CarRented and VehicleStatusChanged events")
    void shouldSuccessfullyRentVehicle() {
        // given — prepare rental aggregate in RESERVED state
        RentalId   rentalId   = RentalId.of("rental-001");
        VehicleId  vehicleId  = VehicleId.of("vehicle-001");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange  period     = DateRange.of(TODAY, IN_7_DAYS);

        Rental reservedRental = Rental.create(rentalId, vehicleId, customerId, period);
        reservedRental.confirm();

        // given — prepare Vehicle aggregate in AVAILABLE state (reconstitution)
        Vehicle availableVehicle = Vehicle.reconstitute(vehicleId, VehicleStatus.AVAILABLE);

        // given — prepare eligible Customer snapshot
        Customer eligibleCustomer = Customer.eligible(customerId);

        // given — wire up repository stubs
        when(rentalRepository.findById(rentalId)).thenReturn(reservedRental);
        when(vehicleRepository.findById(vehicleId)).thenReturn(availableVehicle);
        when(customerRepository.findById(customerId)).thenReturn(eligibleCustomer);

        RentVehicleCommand command = new RentVehicleCommand(rentalId);

        // when
        sut.handle(command);

        // then — Rental aggregate must be re-persisted after activate()
        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());

        Rental savedRental = rentalCaptor.getValue();
        assertThat(savedRental.getStatus()).isEqualTo(RentalStatus.ACTIVE);

        // then — Vehicle aggregate must be re-persisted after rent()
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository, times(1)).save(vehicleCaptor.capture());

        Vehicle savedVehicle = vehicleCaptor.getValue();
        assertThat(savedVehicle.getStatus()).isEqualTo(VehicleStatus.RENTED);

        // then — IEventPublisher must publish events from both aggregates
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());

        List<DomainEvent> publishedEvents = eventCaptor.getAllValues();

        // CarRented originates from Rental.activate()
        assertThat(publishedEvents)
                .filteredOn(e -> e instanceof CarRented)
                .hasSize(1);

        // VehicleStatusChanged originates from Vehicle.rent()
        assertThat(publishedEvents)
                .filteredOn(e -> e instanceof VehicleStatusChanged)
                .hasSize(1);

        VehicleStatusChanged statusChangedEvent = publishedEvents.stream()
                .filter(e -> e instanceof VehicleStatusChanged)
                .map(e -> (VehicleStatusChanged) e)
                .findFirst()
                .orElseThrow();

        assertThat(statusChangedEvent.getVehicleId()).isEqualTo(vehicleId);
        assertThat(statusChangedEvent.getNewStatus()).isEqualTo(VehicleStatus.RENTED);
    }
}