package com.billing.adapters.out.kafka;

import com.billing.domain.CustomerId;
import com.billing.domain.Money;
import com.billing.domain.RentalId;
import com.billing.ports.out.ICostCalculatedEventPublisher;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.CostCalculatedEvent;
import com.rental.events.EventTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class KafkaCostCalculatedEventPublisher implements ICostCalculatedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaCostCalculatedEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaCostCalculatedEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(RentalId rentalId, CustomerId customerId, Money total) {
        CostCalculatedEvent event = new CostCalculatedEvent(
                rentalId.getValue(),
                customerId.getValue(),
                total.toMinorUnits(),
                total.getCurrency());
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(EventTopics.BILLING_EVENTS, rentalId.getValue(), payload);
            log.info("Published CostCalculated to {}", EventTopics.BILLING_EVENTS);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize CostCalculatedEvent", e);
        }
    }
}
