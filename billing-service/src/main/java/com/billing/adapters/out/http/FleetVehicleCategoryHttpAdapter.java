package com.billing.adapters.out.http;

import com.billing.adapters.out.resolvers.HardcodedVehicleCategoryResolver;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.IVehicleCategoryResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Primary
public class FleetVehicleCategoryHttpAdapter implements IVehicleCategoryResolver {

    private final String fleetServiceUrl;
    private final ObjectMapper objectMapper;
    private final HardcodedVehicleCategoryResolver fallback;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public FleetVehicleCategoryHttpAdapter(
            ObjectMapper objectMapper,
            HardcodedVehicleCategoryResolver fallback,
            @Value("${billing.fleet-service-url:http://localhost:8082}") String fleetServiceUrl) {
        this.objectMapper = objectMapper;
        this.fallback = fallback;
        this.fleetServiceUrl = fleetServiceUrl;
    }

    @Override
    public VehicleCategory resolve(String vehicleId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fleetServiceUrl + "/api/v1/vehicles/" + vehicleId))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode body = objectMapper.readTree(response.body());
                if (body.hasNonNull("category")) {
                    return VehicleCategory.valueOf(body.get("category").asText());
                }
            }
        } catch (Exception ignored) {
            /* fallback */
        }
        return fallback.resolve(vehicleId);
    }
}
