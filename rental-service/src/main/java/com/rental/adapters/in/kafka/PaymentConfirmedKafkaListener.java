package com.rental.adapters.in.kafka;

import tools.jackson.databind.ObjectMapper;
import com.rental.application.CloseSettlementUseCase;
import com.rental.domain.RentalId;
import com.rental.events.EventTopics;
import com.rental.events.PaymentConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConfirmedKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentConfirmedKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final CloseSettlementUseCase closeSettlementUseCase;

    public PaymentConfirmedKafkaListener(
            ObjectMapper objectMapper, CloseSettlementUseCase closeSettlementUseCase) {
        this.objectMapper = objectMapper;
        this.closeSettlementUseCase = closeSettlementUseCase;
    }

    @KafkaListener(topics = EventTopics.BILLING_EVENTS, groupId = "rental-service-payment")
    @ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
    public void onKafka(String payload) {
        onPaymentConfirmed(payload);
    }

    public void onPaymentConfirmed(String payload) {
        try {
            PaymentConfirmedEvent event = objectMapper.readValue(payload, PaymentConfirmedEvent.class);
            if (!"PaymentConfirmed".equals(event.getEventType())) {
                return;
            }
            closeSettlementUseCase.handle(RentalId.of(event.getRentalId()));
            log.info("Settlement closed for rental {}", event.getRentalId());
        } catch (Exception e) {
            log.error("Failed to process PaymentConfirmed", e);
            throw new IllegalStateException("Failed to process PaymentConfirmed", e);
        }
    }
}
