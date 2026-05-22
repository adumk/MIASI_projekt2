package com.rental.application;

import com.rental.domain.CarReturned;
import com.rental.domain.VehicleStatusChanged;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
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
    private IVehicleRepository vehicleRepository;

    @Mock
    private IEventPublisher eventPublisher;

    @InjectMocks
    private ReturnVehicleUseCase sut;

    private static final LocalDate TODAY            = LocalDate.now();
    private static final LocalDate IN_7_DAYS        = TODAY.plusDays(7);
    private static final LocalDate ACTUAL_RETURN_ON = IN_7_DAYS.plusDays(1);

    @Test
    @DisplayName("Should complete rental, persist rental and vehicle updates, and publish CarReturned and VehicleStatusChanged events")
    void shouldSuccessfullyReturnVehicle() {
        // given — przygotowanie agregatów przy użyciu precyzyjnego nazewnictwa i rekonstytucji
        RentalId rentalId = RentalId.of("rental-001");
        VehicleId vehicleId = VehicleId.of("vehicle-001");
        CustomerId customerId = CustomerId.of("customer-001");
        DateRange period = DateRange.of(TODAY, IN_7_DAYS);

        Rental activeRental = Rental.create(rentalId, vehicleId, customerId, period);
        activeRental.confirm();
        activeRental.activate();

        // Rekonstytucja stanu zamiast "withStatus"
        Vehicle rentedVehicle = Vehicle.reconstitute(vehicleId, VehicleStatus.RENTED);

        when(rentalRepository.findById(rentalId)).thenReturn(activeRental);
        when(vehicleRepository.findById(vehicleId)).thenReturn(rentedVehicle);

        ReturnVehicleCommand command = new ReturnVehicleCommand(rentalId, ACTUAL_RETURN_ON);

        // when
        sut.handle(command);

        // then — Weryfikacja zapisu Rental
        ArgumentCaptor<Rental> rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository, times(1)).save(rentalCaptor.capture());
        assertThat(rentalCaptor.getValue().getStatus()).isEqualTo(RentalStatus.COMPLETED);

        // then — Weryfikacja zapisu Vehicle
        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository, times(1)).save(vehicleCaptor.capture());
        assertThat(vehicleCaptor.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);

        // then — Weryfikacja publikacji POPRAWNYCH zdarzeń (Rental + Fleet)
        verify(eventPublisher, times(1)).publish(any(CarReturned.class));
        verify(eventPublisher, times(1)).publish(any(VehicleStatusChanged.class));

        // BRAK asercji dla CostCalculated — to domena serwisu Billing!
    }
}