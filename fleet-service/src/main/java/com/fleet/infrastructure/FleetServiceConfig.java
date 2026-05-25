package com.fleet.infrastructure;

import com.fleet.application.AddVehicleUseCase;
import com.fleet.application.CompleteMaintenanceUseCase;
import com.fleet.application.RemoveVehicleUseCase;
import com.fleet.application.ReportDamageUseCase;
import com.fleet.application.ScheduleMaintenanceUseCase;
import com.fleet.application.SearchVehiclesUseCase;
import com.fleet.application.UpdateVehicleStatusUseCase;
import com.fleet.ports.out.IEventPublisher;
import com.fleet.ports.out.IVehicleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FleetServiceConfig {

    @Bean
    AddVehicleUseCase addVehicleUseCase(IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        return new AddVehicleUseCase(vehicleRepository, eventPublisher);
    }

    @Bean
    ReportDamageUseCase reportDamageUseCase(IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        return new ReportDamageUseCase(vehicleRepository, eventPublisher);
    }

    @Bean
    UpdateVehicleStatusUseCase updateVehicleStatusUseCase(
            IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        return new UpdateVehicleStatusUseCase(vehicleRepository, eventPublisher);
    }

    @Bean
    ScheduleMaintenanceUseCase scheduleMaintenanceUseCase(
            IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        return new ScheduleMaintenanceUseCase(vehicleRepository, eventPublisher);
    }

    @Bean
    CompleteMaintenanceUseCase completeMaintenanceUseCase(
            IVehicleRepository vehicleRepository, IEventPublisher eventPublisher) {
        return new CompleteMaintenanceUseCase(vehicleRepository, eventPublisher);
    }

    @Bean
    RemoveVehicleUseCase removeVehicleUseCase(IVehicleRepository vehicleRepository) {
        return new RemoveVehicleUseCase(vehicleRepository);
    }

    @Bean
    SearchVehiclesUseCase searchVehiclesUseCase(IVehicleRepository vehicleRepository) {
        return new SearchVehiclesUseCase(vehicleRepository);
    }
}
