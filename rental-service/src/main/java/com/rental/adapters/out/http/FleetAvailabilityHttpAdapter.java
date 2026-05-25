package com.rental.adapters.out.http;

import com.rental.domain.DateRange;
import com.rental.domain.VehicleId;
import com.rental.domain.VehicleNotAvailableException;
import com.rental.ports.out.IFleetAvailabilityPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class FleetAvailabilityHttpAdapter implements IFleetAvailabilityPort {

    private final WebClient webClient;

    public FleetAvailabilityHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${rental.fleet-service-url:http://localhost:8082}") String fleetServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(fleetServiceUrl).build();
    }

    @Override
    public boolean isAvailable(VehicleId vehicleId, DateRange period) {
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> response = webClient.get()
                    .uri("/api/v1/vehicles/{id}", vehicleId.getValue())
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .block();
            if (response == null) {
                return false;
            }
            String status = String.valueOf(response.get("status"));
            return "AVAILABLE".equals(status);
        } catch (Exception ex) {
            throw new VehicleNotAvailableException("Unable to verify vehicle availability: " + ex.getMessage());
        }
    }
}
