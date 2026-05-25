package com.billing.adapters.out.http;

import com.billing.domain.RentalId;
import com.billing.ports.out.IRentalSnapshotPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class RentalSnapshotHttpAdapter implements IRentalSnapshotPort {

    private final String rentalServiceUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public RentalSnapshotHttpAdapter(
            ObjectMapper objectMapper,
            @Value("${billing.rental-service-url:http://localhost:8081}") String rentalServiceUrl) {
        this.objectMapper = objectMapper;
        this.rentalServiceUrl = rentalServiceUrl;
    }

    @Override
    public Optional<RentalSnapshot> findById(RentalId rentalId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rentalServiceUrl + "/api/v1/rentals/" + rentalId.getValue()))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            JsonNode body = objectMapper.readTree(response.body());
            return Optional.of(new RentalSnapshot(
                    body.get("rentalId").asText(),
                    body.get("vehicleId").asText(),
                    body.get("customerId").asText(),
                    LocalDate.parse(body.get("periodStart").asText()),
                    LocalDate.parse(body.get("periodEnd").asText()),
                    body.get("status").asText()));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
