package com.notification.adapters.in.kafka;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.EventTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaListener.class);

    private final ObjectMapper objectMapper;

    public NotificationKafkaListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = {EventTopics.CAR_EVENTS, EventTopics.BILLING_EVENTS},
            groupId = "notification-service")
    public void onEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            String eventType = root.path("eventType").asText();
            String rentalId = root.path("rentalId").asText("");
            String customerId = root.path("customerId").asText("");
            log.info("NOTIFICATION [{}] rental={} customer={} — email/SMS dispatched (stub)",
                    eventType, rentalId, customerId);
        } catch (Exception ex) {
            log.error("Failed to process notification event", ex);
        }
    }
}
