package com.rental.application;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.ReservationCreated;
import com.rental.domain.VehicleId;
import com.rental.domain.VehicleNotAvailableException;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IFleetAvailabilityPort;
import com.rental.ports.out.IRentalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateReservationUseCase — orchestration and side-effect verification")
class CreateReservationUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    @Mock
    private IFleetAvailabilityPort fleetAvailabilityPort;

    @Mock
    private IEventPublisher eventPublisher;

    @InjectMocks
    private CreateReservationUseCase sut;

    private static final LocalDate TODAY     = LocalDate.now();
    private static final LocalDate IN_7_DAYS = TODAY.plusDays(7);

    @Test
    @DisplayName("Should save rental and publish ReservationCreated event when vehicle is available")
    void shouldSuccessfullyCreateReservation() {
        VehicleId vehicleId   = VehicleId.of("vehicle-001");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange period      = DateRange.of(TODAY, IN_7_DAYS);

        when(fleetAvailabilityPort.isAvailable(vehicleId, period)).thenReturn(true);

        CreateReservationCommand command = new CreateReservationCommand(customerId, vehicleId, period);

        sut.handle(command);

        verify(rentalRepository, times(1)).save(any(Rental.class));

        ArgumentCaptor<ReservationCreated> eventCaptor =
                ArgumentCaptor.forClass(ReservationCreated.class);

        verify(eventPublisher, times(1)).publish(eventCaptor.capture());

        ReservationCreated publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getVehicleId()).isEqualTo(vehicleId);
        assertThat(publishedEvent.getCustomerId()).isEqualTo(customerId);
        assertThat(publishedEvent.getPeriod()).isEqualTo(period);
    }

    @Test
    @DisplayName("Should throw VehicleNotAvailableException when vehicle is not available")
    void shouldRejectReservationWhenVehicleIsUnavailable() {
        VehicleId vehicleId   = VehicleId.of("vehicle-002");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange period      = DateRange.of(TODAY, IN_7_DAYS);

        when(fleetAvailabilityPort.isAvailable(vehicleId, period)).thenReturn(false);

        CreateReservationCommand command = new CreateReservationCommand(customerId, vehicleId, period);

        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(VehicleNotAvailableException.class);

        verify(rentalRepository, times(0)).save(any(Rental.class));
    }
}
