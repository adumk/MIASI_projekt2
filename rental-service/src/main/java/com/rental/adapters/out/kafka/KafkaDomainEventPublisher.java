package com.rental.adapters.out.kafka;

import tools.jackson.databind.ObjectMapper;
import com.rental.domain.CarRented;
import com.rental.domain.CarReturned;
import com.rental.domain.DomainEvent;
import com.rental.domain.RentalCancelled;
import com.rental.domain.ReservationCreated;
import com.rental.events.CarRentedEvent;
import com.rental.events.CarReturnedEvent;
import com.rental.events.EventTopics;
import com.rental.events.RentalCancelledEvent;
import com.rental.events.ReservationCreatedEvent;
import com.rental.ports.out.IEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rental.events.publisher", havingValue = "kafka", matchIfMissing = true)
public class KafkaDomainEventPublisher implements IEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            Object integrationEvent = toIntegrationEvent(event);
            String payload = objectMapper.writeValueAsString(integrationEvent);
            kafkaTemplate.send(EventTopics.CAR_EVENTS, payload);
            log.info("Published {} to {}", event.getClass().getSimpleName(), EventTopics.CAR_EVENTS);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to publish domain event", ex);
        }
    }

    private Object toIntegrationEvent(DomainEvent event) {
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
                    cancelled.getRentalId().getValue(),
                    cancelled.getVehicleId().getValue());
        }
        return event;
    }
}
