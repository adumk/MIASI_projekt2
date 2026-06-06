package com.rental.application;

import com.rental.domain.Customer;
import com.rental.domain.CustomerNotEligibleException;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.ports.out.ICustomerVerificationPort;
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
@DisplayName("ConfirmReservationUseCase — payment confirmation and eligibility check")
class ConfirmReservationUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    @Mock
    private ICustomerVerificationPort customerVerificationPort;

    private ConfirmReservationUseCase useCase;

    private RentalId rentalId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        useCase = new ConfirmReservationUseCase(rentalRepository, customerVerificationPort);
        rentalId = RentalId.of("rental-001");
        customerId = CustomerId.of("customer-001");
    }

    @Test
    @DisplayName("Should confirm payment and save rental when customer is eligible")
    void shouldConfirmPaymentAndSaveRentalWhenCustomerIsEligible() {
        // given
        Rental rental = Rental.reconstitute(
                rentalId,
                VehicleId.of("vehicle-001"),
                customerId,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)),
                RentalStatus.RESERVED,
                false
        );
        when(rentalRepository.findById(rentalId)).thenReturn(rental);
        when(customerVerificationPort.findEligibleCustomer(customerId))
                .thenReturn(Customer.eligible(customerId));

        // when
        useCase.handle(new ConfirmReservationCommand(rentalId));

        // then
        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(captor.capture());
        assertThat(captor.getValue().isPaymentConfirmed()).isTrue();
    }

    @Test
    @DisplayName("Should throw when rental not found")
    void shouldThrowWhenRentalNotFound() {
        // given
        when(rentalRepository.findById(rentalId)).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new ConfirmReservationCommand(rentalId)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should throw CustomerNotEligibleException and never save when customer is not eligible")
    void shouldThrowWhenCustomerNotEligible() {
        // given
        Rental rental = Rental.reconstitute(
                rentalId,
                VehicleId.of("vehicle-001"),
                customerId,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3)),
                RentalStatus.RESERVED,
                false
        );
        when(rentalRepository.findById(rentalId)).thenReturn(rental);
        when(customerVerificationPort.findEligibleCustomer(customerId))
                .thenThrow(new CustomerNotEligibleException("Customer is blocked"));

        // when + then
        assertThatThrownBy(() -> useCase.handle(new ConfirmReservationCommand(rentalId)))
                .isInstanceOf(CustomerNotEligibleException.class);

        verify(rentalRepository, never()).save(any());
    }
}