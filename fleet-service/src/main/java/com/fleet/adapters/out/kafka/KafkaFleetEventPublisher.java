package com.fleet.adapters.out.kafka;

import tools.jackson.databind.ObjectMapper;
import com.fleet.domain.DamageReported;
import com.fleet.domain.DomainEvent;
import com.fleet.ports.out.IEventPublisher;
import com.rental.events.DamageReportedEvent;
import com.rental.events.EventTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "fleet.events.publisher", havingValue = "kafka")
@ConditionalOnBean(KafkaTemplate.class)
public class KafkaFleetEventPublisher implements IEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaFleetEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public KafkaFleetEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event published: {}", event.getClass().getSimpleName());
        if (event instanceof DamageReported damage) {
            try {
                DamageReportedEvent integration = new DamageReportedEvent(
                        damage.getVehicleId().getValue(),
                        damage.getDescription(),
                        damage.getSeverity().name());
                kafkaTemplate.send(
                        EventTopics.CAR_EVENTS,
                        objectMapper.writeValueAsString(integration));
                log.info("Published DamageReported to {}", EventTopics.CAR_EVENTS);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to publish damage event", ex);
            }
        }
    }
}
