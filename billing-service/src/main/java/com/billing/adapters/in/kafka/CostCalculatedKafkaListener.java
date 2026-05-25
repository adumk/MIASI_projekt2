package com.billing.adapters.in.kafka;

import com.billing.application.GenerateInvoiceCommand;
import com.billing.application.GenerateInvoiceUseCase;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.billing.domain.RentalId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CostCalculatedKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CostCalculatedKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final GenerateInvoiceUseCase generateInvoiceUseCase;

    public CostCalculatedKafkaListener(
            ObjectMapper objectMapper, GenerateInvoiceUseCase generateInvoiceUseCase) {
        this.objectMapper = objectMapper;
        this.generateInvoiceUseCase = generateInvoiceUseCase;
    }

    public void onCostCalculated(String payload) {
        try {
            JsonNode root = IntegrationEventJson.read(objectMapper, payload);
            if (!"CostCalculated".equals(IntegrationEventJson.text(root, "eventType"))) {
                return;
            }
            String rentalId = IntegrationEventJson.text(root, "rentalId");
            generateInvoiceUseCase.handle(new GenerateInvoiceCommand(RentalId.of(rentalId)));
            log.info("Invoice generated after CostCalculated for {}", rentalId);
        } catch (Exception e) {
            log.error("Failed to process CostCalculated", e);
            throw new IllegalStateException("Failed to process CostCalculated", e);
        }
    }
}
