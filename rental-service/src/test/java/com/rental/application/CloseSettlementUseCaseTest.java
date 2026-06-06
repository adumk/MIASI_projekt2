package com.rental.application;

import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.InvalidStatusTransitionException;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.domain.Money;
import com.rental.ports.out.IRentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloseSettlementUseCase — closing settlement for completed rentals")
class CloseSettlementUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    private CloseSettlementUseCase useCase;

    private RentalId rentalId;
    private CustomerId customerId;
    private VehicleId vehicleId;

    @BeforeEach
    void setUp() {
        useCase = new CloseSettlementUseCase(rentalRepository);
        rentalId = RentalId.of("rental-001");
        customerId = CustomerId.of("customer-001");
        vehicleId = VehicleId.of("vehicle-001");
    }

    @Test
    @DisplayName("Should close settlement for a COMPLETED rental and save it")
    void shouldCloseSettlementForCompletedRental() {
        // given
        DateRange period = DateRange.ofHistorical(
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
        Rental rental = Rental.create(rentalId, vehicleId, customerId, period);
        rental.confirm();
        rental.confirmPayment();
        rental.activate(Customer.eligible(customerId));
        rental.complete(Money.of(450, "PLN"), LocalDate.now().minusDays(1), 45000, null);

        when(rentalRepository.findById(rentalId)).thenReturn(rental);

        // when
        useCase.handle(rentalId);

        // then
        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(captor.capture());
        assertThat(captor.getValue().isSettlementClosed()).isTrue();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when rental not found")
    void shouldThrowWhenRentalNotFound() {
        // given
        when(rentalRepository.findById(rentalId)).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> useCase.handle(rentalId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(rentalId.getValue());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException and never save when rental is not COMPLETED")
    void shouldThrowWhenRentalNotInCompletedState() {
        // given
        Rental rental = Rental.reconstitute(
                rentalId, vehicleId, customerId,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)),
                RentalStatus.RESERVED
        );
        when(rentalRepository.findById(rentalId)).thenReturn(rental);

        // when + then
        assertThatThrownBy(() -> useCase.handle(rentalId))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(rentalRepository, never()).save(any());
    }
}