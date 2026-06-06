package com.rental.application;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetRentalHistoryUseCase — retrieving rental history by customer")
class GetRentalHistoryUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    private GetRentalHistoryUseCase useCase;

    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        useCase = new GetRentalHistoryUseCase(rentalRepository);
        customerId = CustomerId.of("customer-001");
    }

    @Test
    @DisplayName("Should return rental history for a given customer")
    void shouldReturnRentalHistoryForCustomer() {
        // given
        DateRange period = DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3));
        List<Rental> rentals = List.of(
                Rental.reconstitute(RentalId.of("r-1"), VehicleId.of("v-1"), customerId, period, RentalStatus.COMPLETED),
                Rental.reconstitute(RentalId.of("r-2"), VehicleId.of("v-2"), customerId, period, RentalStatus.CANCELLED),
                Rental.reconstitute(RentalId.of("r-3"), VehicleId.of("v-3"), customerId, period, RentalStatus.ACTIVE)
        );
        when(rentalRepository.findByCustomerId(customerId)).thenReturn(rentals);

        // when
        List<Rental> result = useCase.handle(new GetRentalHistoryQuery(customerId));

        // then
        assertThat(result).hasSize(3);
        verify(rentalRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("Should return empty list when no rentals found for customer")
    void shouldReturnEmptyListWhenNoRentalsFound() {
        // given
        when(rentalRepository.findByCustomerId(customerId)).thenReturn(List.of());

        // when
        List<Rental> result = useCase.handle(new GetRentalHistoryQuery(customerId));

        // then
        assertThat(result).isEmpty();
    }
}