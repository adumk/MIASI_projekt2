package com.fleet.adapters.out.events;

import com.fleet.domain.DamageReported;
import com.fleet.domain.DomainEvent;
import com.fleet.ports.out.IEventPublisher;
import com.rental.events.DamageReportedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Profile("local")
@Primary
public class LocalForwardingFleetEventPublisher implements IEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalForwardingFleetEventPublisher.class);

    private final LoggingEventPublisher loggingEventPublisher;
    private final String billingBaseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public LocalForwardingFleetEventPublisher(
            LoggingEventPublisher loggingEventPublisher,
            ObjectMapper objectMapper,
            @Value("${fleet.billing-service-url:http://localhost:8084}") String billingUrl) {
        this.loggingEventPublisher = loggingEventPublisher;
        this.billingBaseUrl = billingUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        loggingEventPublisher.publish(event);
        if (event instanceof DamageReported damage) {
            try {
                DamageReportedEvent integration = new DamageReportedEvent(
                        damage.getVehicleId().getValue(),
                        damage.getDescription(),
                        damage.getSeverity().name());
                String payload = objectMapper.writeValueAsString(integration);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(billingBaseUrl + "/internal/integration-events"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception ex) {
                log.warn("Failed to forward damage event: {}", ex.getMessage());
            }
        }
    }
}
