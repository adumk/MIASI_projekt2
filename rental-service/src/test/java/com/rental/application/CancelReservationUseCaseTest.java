package com.rental.application;

import com.rental.domain.*;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IRentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelReservationUseCaseTest {

    @Mock private IRentalRepository rentalRepository;
    @Mock private IEventPublisher eventPublisher;

    @InjectMocks private CancelReservationUseCase useCase;

    @Test
    @DisplayName("Should change state to CANCELLED, save aggregate and publish event during happy path cancellation")
    void shouldCancelActiveReservationAndPublishEvent() {
        // given
        RentalId targetRentalId = RentalId.of("RESERVATION-123");
        Rental activeReservation = Rental.reconstitute(targetRentalId, RentalStatus.RESERVED);

        when(rentalRepository.findById(targetRentalId)).thenReturn(activeReservation);
        CancelReservationCommand command = new CancelReservationCommand(targetRentalId);

        // when
        useCase.handle(command);

        // then
        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());
        assertThat(rentalCaptor.getValue().getStatus()).isEqualTo(RentalStatus.CANCELLED);

        verify(eventPublisher, times(1)).publish(any(RentalCancelled.class));
    }
}