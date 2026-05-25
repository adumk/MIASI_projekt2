package com.rental.adapters.out.http;

import com.rental.domain.VehicleId;
import com.rental.ports.out.IFleetVehicleInfoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class FleetVehicleInfoHttpAdapter implements IFleetVehicleInfoPort {

    private final WebClient webClient;

    public FleetVehicleInfoHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${rental.fleet-service-url:http://localhost:8082}") String fleetServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(fleetServiceUrl).build();
    }

    @Override
    public String resolveCategory(VehicleId vehicleId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.get()
                .uri("/api/v1/vehicles/{id}", vehicleId.getValue())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (response == null || response.get("category") == null) {
            return "COMPACT";
        }
        return String.valueOf(response.get("category"));
    }
}
