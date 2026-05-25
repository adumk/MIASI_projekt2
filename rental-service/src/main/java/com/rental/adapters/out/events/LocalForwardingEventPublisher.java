package com.rental.adapters.out.events;

import tools.jackson.databind.ObjectMapper;
import com.rental.domain.CarRented;
import com.rental.domain.CarReturned;
import com.rental.domain.DomainEvent;
import com.rental.domain.RentalCancelled;
import com.rental.domain.ReservationCreated;
import com.rental.events.CarRentedEvent;
import com.rental.events.CarReturnedEvent;
import com.rental.events.RentalCancelledEvent;
import com.rental.events.ReservationCreatedEvent;
import com.rental.ports.out.IEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnProperty(name = "rental.events.publisher", havingValue = "logging")
public class LocalForwardingEventPublisher implements IEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalForwardingEventPublisher.class);

    private final WebClient fleetClient;
    private final WebClient billingClient;
    private final WebClient notificationClient;
    private final ObjectMapper objectMapper;

    public LocalForwardingEventPublisher(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${rental.fleet-service-url:http://localhost:8082}") String fleetUrl,
            @Value("${rental.billing-service-url:http://localhost:8084}") String billingUrl,
            @Value("${rental.notification-service-url:http://localhost:8085}") String notificationUrl) {
        this.fleetClient = webClientBuilder.baseUrl(fleetUrl).build();
        this.billingClient = webClientBuilder.baseUrl(billingUrl).build();
        this.notificationClient = webClientBuilder.baseUrl(notificationUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event published: {}", event.getClass().getSimpleName());
        try {
            Object integration = toIntegration(event);
            String payload = objectMapper.writeValueAsString(integration);
            if (event instanceof ReservationCreated || event instanceof CarRented
                    || event instanceof CarReturned || event instanceof RentalCancelled) {
                fleetClient.post()
                        .uri("/internal/integration-events")
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .subscribe();
            }
            if (event instanceof CarReturned || event instanceof CarRented || event instanceof ReservationCreated) {
                billingClient.post()
                        .uri("/internal/integration-events")
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .subscribe();
                notificationClient.post()
                        .uri("/internal/integration-events")
                        .bodyValue(payload)
                        .retrieve()
                        .toBodilessEntity()
                        .onErrorResume(e -> {
                            log.debug("Notification service unavailable: {}", e.getMessage());
                            return reactor.core.publisher.Mono.empty();
                        })
                        .subscribe();
            }
        } catch (Exception ex) {
            log.warn("Failed to forward event locally: {}", ex.getMessage());
        }
    }

    private Object toIntegration(DomainEvent event) {
        if (event instanceof ReservationCreated created) {
            return new ReservationCreatedEvent(
                    created.getRentalId().getValue(),
                    created.getVehicleId().getValue(),
                    created.getCustomerId().getValue(),
                    created.getPeriod().getStart().toString(),
                    created.getPeriod().getEnd().toString());
        }
        if (event instanceof CarRented rented) {
            return new CarRentedEvent(
                    rented.getRentalId().getValue(),
                    rented.getVehicleId().getValue(),
                    rented.getCustomerId().getValue(),
                    rented.getActualStartDate().toString());
        }
        if (event instanceof CarReturned returned) {
            return new CarReturnedEvent(
                    returned.getRentalId().getValue(),
                    returned.getVehicleId().getValue(),
                    returned.getCustomerId().getValue(),
                    returned.getReturnDate().toString(),
                    returned.getPeriodStart().toString(),
                    null,
                    returned.getFinalCost().getAmount().longValue(),
                    returned.getFinalCost().getCurrency());
        }
        if (event instanceof RentalCancelled cancelled) {
            return new RentalCancelledEvent(
                    cancelled.getRentalId().getValue(), cancelled.getVehicleId().getValue());
        }
        return event;
    }
}
