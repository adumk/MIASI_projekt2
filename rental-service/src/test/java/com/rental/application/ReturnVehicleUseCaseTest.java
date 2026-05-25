package com.rental.application;

import com.rental.domain.CarReturned;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.domain.Money;
import com.rental.ports.out.IBillingQuotePort;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IFleetVehicleInfoPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnVehicleUseCase — state transition, persistence and event publication")
class ReturnVehicleUseCaseTest {

    @Mock
    private IRentalRepository rentalRepository;

    @Mock
    private IEventPublisher eventPublisher;

    @Mock
    private IFleetVehicleInfoPort fleetVehicleInfoPort;

    @Mock
    private IBillingQuotePort billingQuotePort;

    @InjectMocks
    private ReturnVehicleUseCase sut;

    private static final LocalDate TODAY            = LocalDate.now();
    private static final LocalDate IN_7_DAYS        = TODAY.plusDays(7);
    private static final LocalDate ACTUAL_RETURN_ON = IN_7_DAYS.plusDays(1);

    @Test
    @DisplayName("Should complete rental, persist rental and publish CarReturned event")
    void shouldSuccessfullyReturnVehicle() {
        RentalId rentalId = RentalId.of("rental-001");
        VehicleId vehicleId = VehicleId.of("vehicle-001");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange period = DateRange.of(TODAY, IN_7_DAYS);

        Rental activeRental = Rental.create(rentalId, vehicleId, customerId, period);
        activeRental.confirm();
        activeRental.confirmPayment();
        activeRental.activate();

        when(rentalRepository.findById(rentalId)).thenReturn(activeRental);
        when(fleetVehicleInfoPort.resolveCategory(vehicleId)).thenReturn("COMPACT");
        when(billingQuotePort.quoteRentalCost(any(), any(), any())).thenReturn(Money.of(35000, "PLN"));

        sut.handle(new ReturnVehicleCommand(rentalId, ACTUAL_RETURN_ON, 12000, "OK"));

        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());
        assertThat(rentalCaptor.getValue().getStatus()).isEqualTo(RentalStatus.COMPLETED);

        verify(eventPublisher, times(1)).publish(any(CarReturned.class));
    }
}
