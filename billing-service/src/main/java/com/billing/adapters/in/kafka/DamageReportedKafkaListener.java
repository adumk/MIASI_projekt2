package com.billing.adapters.in.kafka;

import com.billing.application.ApplyDamageFeeCommand;
import com.billing.application.ApplyDamageFeeUseCase;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.DamageReportedEvent;
import com.rental.events.EventTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DamageReportedKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(DamageReportedKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final ApplyDamageFeeUseCase applyDamageFeeUseCase;

    public DamageReportedKafkaListener(
            ObjectMapper objectMapper, ApplyDamageFeeUseCase applyDamageFeeUseCase) {
        this.objectMapper = objectMapper;
        this.applyDamageFeeUseCase = applyDamageFeeUseCase;
    }

    public void onDamageReported(String payload) {
        try {
            DamageReportedEvent event = objectMapper.readValue(payload, DamageReportedEvent.class);
            if (!"DamageReported".equals(event.getEventType())) {
                return;
            }
            applyDamageFeeUseCase.handle(new ApplyDamageFeeCommand(event.getVehicleId(), event.getSeverity()));
            log.info("Damage fee applied for vehicle {}", event.getVehicleId());
        } catch (Exception e) {
            log.error("Failed to process DamageReported", e);
            throw new IllegalStateException("Failed to process DamageReported", e);
        }
    }
}
