package com.billing.adapters.out.events;

import com.billing.adapters.in.kafka.CostCalculatedKafkaListener;
import com.billing.domain.CustomerId;
import com.billing.domain.Money;
import com.billing.domain.RentalId;
import com.billing.ports.out.ICostCalculatedEventPublisher;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.CostCalculatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@Primary
public class LocalCostCalculatedEventPublisher implements ICostCalculatedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalCostCalculatedEventPublisher.class);

    private final ObjectMapper objectMapper;
    private final CostCalculatedKafkaListener costCalculatedKafkaListener;

    public LocalCostCalculatedEventPublisher(
            ObjectMapper objectMapper, CostCalculatedKafkaListener costCalculatedKafkaListener) {
        this.objectMapper = objectMapper;
        this.costCalculatedKafkaListener = costCalculatedKafkaListener;
    }

    @Override
    public void publish(RentalId rentalId, CustomerId customerId, Money total) {
        log.info(
                "CostCalculated (local) rental={} customer={} amount={} {}",
                rentalId.getValue(),
                customerId.getValue(),
                total.toMinorUnits(),
                total.getCurrency());
        try {
            CostCalculatedEvent event = new CostCalculatedEvent(
                    rentalId.getValue(),
                    customerId.getValue(),
                    total.toMinorUnits(),
                    total.getCurrency());
            costCalculatedKafkaListener.onCostCalculated(objectMapper.writeValueAsString(event));
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to dispatch CostCalculated locally", e);
        }
    }
}
