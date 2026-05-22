package com.rental.application;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.ReservationCreated;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleId;
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
    private IVehicleRepository vehicleRepository;

    @Mock
    private IEventPublisher eventPublisher;

    @InjectMocks
    private CreateReservationUseCase sut;

    private static final LocalDate TODAY     = LocalDate.now();
    private static final LocalDate IN_7_DAYS = TODAY.plusDays(7);

    // =========================================================================
    // shouldSuccessfullyCreateReservation
    // =========================================================================

    @Test
    @DisplayName("Should save rental and publish ReservationCreated event when vehicle is available")
    void shouldSuccessfullyCreateReservation() {
        // given
        VehicleId vehicleId   = VehicleId.of("vehicle-001");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange period      = DateRange.of(TODAY, IN_7_DAYS);

        Vehicle availableVehicle = Vehicle.withStatus(vehicleId, VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(vehicleId)).thenReturn(availableVehicle);

        CreateReservationCommand command = new CreateReservationCommand(
                customerId,
                vehicleId,
                period
        );

        // when
        sut.handle(command);

        // then — rental must be persisted exactly once
        verify(rentalRepository, times(1)).save(any(Rental.class));

        // then — ReservationCreated domain event must be published
        ArgumentCaptor<ReservationCreated> eventCaptor =
                ArgumentCaptor.forClass(ReservationCreated.class);

        verify(eventPublisher, times(1)).publish(eventCaptor.capture());

        ReservationCreated publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getVehicleId()).isEqualTo(vehicleId);
        assertThat(publishedEvent.getCustomerId()).isEqualTo(customerId);
        assertThat(publishedEvent.getPeriod()).isEqualTo(period);
    }

    // =========================================================================
    // shouldRejectReservationWhenVehicleIsUnavailable
    // =========================================================================

    @Test
    @DisplayName("Should throw VehicleNotAvailableException when vehicle status is not AVAILABLE")
    void shouldRejectReservationWhenVehicleIsUnavailable() {
        // given
        VehicleId vehicleId   = VehicleId.of("vehicle-002");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange period      = DateRange.of(TODAY, IN_7_DAYS);

        Vehicle unavailableVehicle = Vehicle.withStatus(vehicleId, VehicleStatus.RENTED);
        when(vehicleRepository.findById(vehicleId)).thenReturn(unavailableVehicle);

        CreateReservationCommand command = new CreateReservationCommand(
                customerId,
                vehicleId,
                period
        );

        // when + then
        assertThatThrownBy(() -> sut.handle(command))
                .isInstanceOf(VehicleNotAvailableException.class);

        // then — no rental must be persisted on failure
        verify(rentalRepository, times(0)).save(any(Rental.class));
    }
}