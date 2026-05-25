package com.fleet.infrastructure;

import com.fleet.application.AddVehicleCommand;
import com.fleet.application.AddVehicleUseCase;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.ports.out.IVehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class LocalFleetDataInitializer {

    @Bean
    CommandLineRunner seedFleet(AddVehicleUseCase addVehicleUseCase, IVehicleRepository vehicleRepository) {
        return args -> {
            if (vehicleRepository.findById(VehicleId.of("vehicle-001")) == null) {
                addVehicleUseCase.handle(new AddVehicleCommand(
                        VehicleId.of("vehicle-001"),
                        "WA12345",
                        "Toyota",
                        "Corolla",
                        2022,
                        VehicleCategory.STANDARD));
            }
        };
    }
}
