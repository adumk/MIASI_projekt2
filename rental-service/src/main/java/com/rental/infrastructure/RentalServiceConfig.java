package com.rental.infrastructure;

import com.rental.application.CancelReservationUseCase;
import com.rental.application.CloseSettlementUseCase;
import com.rental.application.ConfirmReservationUseCase;
import com.rental.application.CreateReservationUseCase;
import com.rental.application.GetRentalHistoryUseCase;
import com.rental.application.MarkOverdueUseCase;
import com.rental.application.RentVehicleUseCase;
import com.rental.application.ReturnVehicleUseCase;
import com.rental.ports.out.IBillingQuotePort;
import com.rental.ports.out.ICustomerVerificationPort;
import com.rental.ports.out.IEventPublisher;
import com.rental.ports.out.IFleetAvailabilityPort;
import com.rental.ports.out.IFleetVehicleInfoPort;
import com.rental.ports.out.IRentalRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RentalServiceConfig {

    @Bean
    CreateReservationUseCase createReservationUseCase(
            IRentalRepository rentalRepository,
            IFleetAvailabilityPort fleetAvailabilityPort,
            IEventPublisher eventPublisher) {
        return new CreateReservationUseCase(rentalRepository, fleetAvailabilityPort, eventPublisher);
    }

    @Bean
    RentVehicleUseCase rentVehicleUseCase(
            IRentalRepository rentalRepository,
            ICustomerVerificationPort customerVerificationPort,
            IEventPublisher eventPublisher) {
        return new RentVehicleUseCase(rentalRepository, customerVerificationPort, eventPublisher);
    }

    @Bean
    ReturnVehicleUseCase returnVehicleUseCase(
            IRentalRepository rentalRepository,
            IEventPublisher eventPublisher,
            IFleetVehicleInfoPort fleetVehicleInfoPort,
            IBillingQuotePort billingQuotePort) {
        return new ReturnVehicleUseCase(
                rentalRepository, eventPublisher, fleetVehicleInfoPort, billingQuotePort);
    }

    @Bean
    CancelReservationUseCase cancelReservationUseCase(
            IRentalRepository rentalRepository, IEventPublisher eventPublisher) {
        return new CancelReservationUseCase(rentalRepository, eventPublisher);
    }

    @Bean
    GetRentalHistoryUseCase getRentalHistoryUseCase(IRentalRepository rentalRepository) {
        return new GetRentalHistoryUseCase(rentalRepository);
    }

    @Bean
    ConfirmReservationUseCase confirmReservationUseCase(
            IRentalRepository rentalRepository,
            ICustomerVerificationPort customerVerificationPort) {
        return new ConfirmReservationUseCase(rentalRepository, customerVerificationPort);
    }

    @Bean
    MarkOverdueUseCase markOverdueUseCase(IRentalRepository rentalRepository) {
        return new MarkOverdueUseCase(rentalRepository);
    }

    @Bean
    CloseSettlementUseCase closeSettlementUseCase(IRentalRepository rentalRepository) {
        return new CloseSettlementUseCase(rentalRepository);
    }
}
