package com.billing.adapters.in.kafka;

import com.billing.domain.RentalId;
import com.billing.ports.out.IRentalBillingSessionStore;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.CarRentedEvent;
import com.rental.events.EventTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CarRentedKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CarRentedKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final IRentalBillingSessionStore sessionStore;

    public CarRentedKafkaListener(ObjectMapper objectMapper, IRentalBillingSessionStore sessionStore) {
        this.objectMapper = objectMapper;
        this.sessionStore = sessionStore;
    }

    public void onCarRented(String payload) {
        try {
            // 1. Najpierw czytamy payload jako surowe drzewo dokumentu JSON
            var jsonNode = objectMapper.readTree(payload);

            // 2. Bezpiecznie wyciągamy rzeczywistą wartość "eventType" z tekstu
            String actualEventType = jsonNode.path("eventType").asText();

            // 3. Jeśli typ się nie zgadza — przerywamy działanie (Guard Clause)
            if (!"CarRented".equals(actualEventType)) {
                return;
            }

            // 4. Dopiero po walidacji typu mapujemy na pełny obiekt biznesowy
            CarRentedEvent event = objectMapper.readValue(payload, CarRentedEvent.class);

            sessionStore.startSession(
                    RentalId.of(event.getRentalId()),
                    LocalDate.parse(event.getActualStartDate()));
            log.info("Billing session started for rental {}", event.getRentalId());

        } catch (Exception e) {
            log.error("Failed to process CarRented", e);
            throw new IllegalStateException("Failed to process CarRented", e);
        }
    }
}
