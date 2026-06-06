package com.rental.application;

import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.ports.out.IRentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkOverdueUseCase — marking expired active rentals as overdue")
class MarkOverdueUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    private MarkOverdueUseCase useCase;

    private CustomerId customerId;
    private VehicleId vehicleId;

    @BeforeEach
    void setUp() {
        useCase = new MarkOverdueUseCase(rentalRepository);
        customerId = CustomerId.of("customer-001");
        vehicleId = VehicleId.of("vehicle-001");
    }

    private Rental activeRentalWithPeriod(String rentalId, DateRange period) {
        Rental rental = Rental.create(RentalId.of(rentalId), vehicleId, customerId, period);
        rental.confirm();
        rental.confirmPayment();
        rental.activate(Customer.eligible(customerId));
        return rental;
    }

    @Test
    @DisplayName("Should mark all expired active rentals as overdue")
    void shouldMarkAllExpiredActiveRentalsAsOverdue() {
        // given
        DateRange expiredPeriod1 = DateRange.ofHistorical(
                LocalDate.now().minusDays(7), LocalDate.now().minusDays(1));
        DateRange expiredPeriod2 = DateRange.ofHistorical(
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(2));
        DateRange validPeriod = DateRange.of(
                LocalDate.now(), LocalDate.now().plusDays(3));

        Rental overdueRental1 = activeRentalWithPeriod("r-1", expiredPeriod1);
        Rental overdueRental2 = activeRentalWithPeriod("r-2", expiredPeriod2);
        Rental activeRental   = activeRentalWithPeriod("r-3", validPeriod);

        when(rentalRepository.findActiveRentals())
                .thenReturn(List.of(overdueRental1, overdueRental2, activeRental));

        // when
        int count = useCase.handle();

        // then
        assertThat(count).isEqualTo(2);
        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .allMatch(r -> r.getStatus() == RentalStatus.OVERDUE);
    }

    @Test
    @DisplayName("Should return zero when no rentals are overdue")
    void shouldReturnZeroWhenNoRentalsAreOverdue() {
        // given
        DateRange validPeriod = DateRange.of(LocalDate.now(), LocalDate.now().plusDays(5));
        Rental activeRental = activeRentalWithPeriod("r-1", validPeriod);
        when(rentalRepository.findActiveRentals()).thenReturn(List.of(activeRental));

        // when
        int count = useCase.handle();

        // then
        assertThat(count).isEqualTo(0);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return zero when there are no active rentals")
    void shouldReturnZeroWhenNoActiveRentals() {
        // given
        when(rentalRepository.findActiveRentals()).thenReturn(List.of());

        // when
        int count = useCase.handle();

        // then
        assertThat(count).isEqualTo(0);
    }
}