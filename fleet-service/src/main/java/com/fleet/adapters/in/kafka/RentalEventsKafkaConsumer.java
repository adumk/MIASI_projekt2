package com.fleet.adapters.in.kafka;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.CarRentedEvent;
import com.rental.events.CarReturnedEvent;
import com.rental.events.EventTopics;
import com.rental.events.RentalCancelledEvent;
import com.rental.events.ReservationCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class RentalEventsKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(RentalEventsKafkaConsumer.class);

    private final ObjectMapper objectMapper;
    private final ReservationCreatedAclHandler reservationCreatedAclHandler;
    private final CarRentedAclHandler carRentedAclHandler;
    private final CarReturnedAclHandler carReturnedAclHandler;
    private final RentalCancelledAclHandler rentalCancelledAclHandler;

    public RentalEventsKafkaConsumer(
            ObjectMapper objectMapper,
            ReservationCreatedAclHandler reservationCreatedAclHandler,
            CarRentedAclHandler carRentedAclHandler,
            CarReturnedAclHandler carReturnedAclHandler,
            RentalCancelledAclHandler rentalCancelledAclHandler) {
        this.objectMapper = objectMapper;
        this.reservationCreatedAclHandler = reservationCreatedAclHandler;
        this.carRentedAclHandler = carRentedAclHandler;
        this.carReturnedAclHandler = carReturnedAclHandler;
        this.rentalCancelledAclHandler = rentalCancelledAclHandler;
    }

    @KafkaListener(topics = EventTopics.CAR_EVENTS, groupId = "fleet-service")
    public void onCarEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            String eventType = root.path("eventType").asText();
            switch (eventType) {
                case "ReservationCreated" -> reservationCreatedAclHandler.handle(toReservationCreatedEvent(root));
                case "CarRented" -> carRentedAclHandler.handle(toCarRentedEvent(root));
                case "CarReturned" -> carReturnedAclHandler.handle(toCarReturnedEvent(root));
                case "RentalCancelled" -> rentalCancelledAclHandler.handle(toRentalCancelledEvent(root));
                default -> log.warn("Ignoring unsupported event type: {}", eventType);
            }
        } catch (Exception ex) {
            log.error("Failed to process car event from topic {}", record.topic(), ex);
            throw new IllegalStateException("Failed to process car event", ex);
        }
    }

    private ReservationCreatedEvent toReservationCreatedEvent(JsonNode root) {
        return new ReservationCreatedEvent(
                root.path("rentalId").asText(),
                root.path("vehicleId").asText(),
                root.path("customerId").asText(),
                root.path("periodStart").asText(),
                root.path("periodEnd").asText());
    }

    private CarRentedEvent toCarRentedEvent(JsonNode root) {
        return new CarRentedEvent(
                root.path("rentalId").asText(),
                root.path("vehicleId").asText(),
                root.path("customerId").asText(),
                root.path("actualStartDate").asText());
    }

    private CarReturnedEvent toCarReturnedEvent(JsonNode root) {
        return new CarReturnedEvent(
                root.path("rentalId").asText(),
                root.path("vehicleId").asText(),
                root.path("customerId").asText(),
                root.path("returnDate").asText());
    }

    private RentalCancelledEvent toRentalCancelledEvent(JsonNode root) {
        return new RentalCancelledEvent(
                root.path("rentalId").asText(),
                root.path("vehicleId").asText());
    }
}
