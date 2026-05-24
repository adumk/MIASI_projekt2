package com.rental.application;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.InvalidStatusTransitionException;
import com.rental.domain.Rental;
import com.rental.domain.RentalCancelled;
import com.rental.domain.RentalId;
import com.rental.domain.RentalNotFoundException;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelReservationUseCase — orchestration and side-effect verification")
class CancelReservationUseCaseTest {

    @Mock private IRentalRepository rentalRepository;
    @Mock private IEventPublisher   eventPublisher;

    @InjectMocks private CancelReservationUseCase sut;

    private static final RentalId   RENTAL_ID   = RentalId.of(UUID.randomUUID());
    private static final VehicleId  VEHICLE_ID  = VehicleId.of(UUID.randomUUID());
    private static final CustomerId CUSTOMER_ID = CustomerId.of(UUID.randomUUID());
    private static final DateRange  PERIOD      = DateRange.of(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5)
    );

    // -------------------------------------------------------------------------
    // Existing test — preserved, identifiers corrected to UUID-based model
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should change state to CANCELLED, save aggregate and publish event")
    void shouldCancelActiveReservationAndPublishEvent() {
        Rental reserved = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.RESERVED, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reserved);

        sut.handle(new CancelReservationCommand(RENTAL_ID));

        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(RentalStatus.CANCELLED);

        verify(eventPublisher, times(1)).publish(any(RentalCancelled.class));
    }

    // -------------------------------------------------------------------------
    // New tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should throw when rental does not exist in repository")
    void shouldRejectCancellationWhenRentalDoesNotExist() {
        when(rentalRepository.findById(RENTAL_ID))
                .thenThrow(new RentalNotFoundException(RENTAL_ID));

        assertThatThrownBy(() -> sut.handle(new CancelReservationCommand(RENTAL_ID)))
                .isInstanceOf(RentalNotFoundException.class);

        verify(rentalRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should reject cancellation when rental is already ACTIVE")
    void shouldRejectCancellationWhenRentalIsAlreadyActive() {
        Rental active = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.ACTIVE, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(active);

        assertThatThrownBy(() -> sut.handle(new CancelReservationCommand(RENTAL_ID)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(rentalRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should reject cancellation when rental is already COMPLETED")
    void shouldRejectCancellationWhenRentalIsAlreadyCompleted() {
        Rental completed = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.COMPLETED, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(completed);

        assertThatThrownBy(() -> sut.handle(new CancelReservationCommand(RENTAL_ID)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(rentalRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should reject cancellation when rental is already CANCELLED")
    void shouldRejectCancellationWhenRentalIsAlreadyCancelled() {
        Rental cancelled = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.CANCELLED, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(cancelled);

        assertThatThrownBy(() -> sut.handle(new CancelReservationCommand(RENTAL_ID)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(rentalRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("should persist updated rental exactly once on successful cancellation")
    void shouldPersistUpdatedRentalExactlyOnce() {
        Rental reserved = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.RESERVED, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reserved);

        sut.handle(new CancelReservationCommand(RENTAL_ID));

        verify(rentalRepository, times(1)).save(any(Rental.class));
    }

    @Test
    @DisplayName("should publish RentalCancelled event with correct rentalId and vehicleId")
    void shouldPublishRentalCancelledEventWithCorrectPayload() {
        Rental reserved = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.RESERVED, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(reserved);

        sut.handle(new CancelReservationCommand(RENTAL_ID));

        ArgumentCaptor<RentalCancelled> captor = ArgumentCaptor.forClass(RentalCancelled.class);
        verify(eventPublisher, times(1)).publish(captor.capture());

        RentalCancelled published = captor.getValue();
        assertThat(published.rentalId()).isEqualTo(RENTAL_ID);
        assertThat(published.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(published.customerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    @DisplayName("should not publish event when cancellation fails")
    void shouldNotPublishEventOnFailure() {
        Rental active = Rental.reconstitute(RENTAL_ID, VEHICLE_ID, CUSTOMER_ID, PERIOD, RentalStatus.ACTIVE, null);
        when(rentalRepository.findById(RENTAL_ID)).thenReturn(active);

        assertThatThrownBy(() -> sut.handle(new CancelReservationCommand(RENTAL_ID)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(eventPublisher, never()).publish(any());
    }
}