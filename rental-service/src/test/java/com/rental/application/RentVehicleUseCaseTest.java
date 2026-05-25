package com.rental.application;

import com.rental.domain.CarRented;
import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.DomainEvent;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.ports.out.ICustomerVerificationPort;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IRentalRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentVehicleUseCase — state transition, persistence and event publication")
class RentVehicleUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    @Mock
    private ICustomerVerificationPort customerVerificationPort;

    @Mock
    private IEventPublisher eventPublisher;

    @InjectMocks
    private RentVehicleUseCase sut;

    private static final LocalDate TODAY     = LocalDate.now();
    private static final LocalDate IN_7_DAYS = TODAY.plusDays(7);

    @Test
    @DisplayName("Should activate rental, persist aggregate and publish CarRented event")
    void shouldSuccessfullyRentVehicle() {
        RentalId   rentalId   = RentalId.of("rental-001");
        VehicleId  vehicleId  = VehicleId.of("vehicle-001");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange  period     = DateRange.of(TODAY, IN_7_DAYS);

        Rental reservedRental = Rental.reconstitute(rentalId, vehicleId, customerId, period, RentalStatus.RESERVED, true);

        Customer eligibleCustomer = Customer.eligible(customerId);

        when(rentalRepository.findById(rentalId)).thenReturn(reservedRental);
        when(customerVerificationPort.findEligibleCustomer(customerId)).thenReturn(eligibleCustomer);

        sut.handle(new RentVehicleCommand(rentalId));

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());

        Rental savedRental = rentalCaptor.getValue();
        assertThat(savedRental.getStatus()).isEqualTo(RentalStatus.ACTIVE);

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(CarRented.class);

        List<DomainEvent> publishedEvents = eventCaptor.getAllValues();
        assertThat(publishedEvents).filteredOn(e -> e instanceof CarRented).hasSize(1);
    }
}
