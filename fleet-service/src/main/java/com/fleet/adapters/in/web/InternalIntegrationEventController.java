package com.fleet.adapters.in.web;

import com.fleet.adapters.in.kafka.CarRentedAclHandler;
import com.fleet.adapters.in.kafka.CarReturnedAclHandler;
import com.fleet.adapters.in.kafka.RentalCancelledAclHandler;
import com.fleet.adapters.in.kafka.ReservationCreatedAclHandler;
import com.rental.events.CarRentedEvent;
import com.rental.events.CarReturnedEvent;
import com.rental.events.RentalCancelledEvent;
import com.rental.events.ReservationCreatedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@Profile("local")
@RequestMapping("/internal/integration-events")
public class InternalIntegrationEventController {

    private final ObjectMapper objectMapper;
    private final ReservationCreatedAclHandler reservationCreatedAclHandler;
    private final CarRentedAclHandler carRentedAclHandler;
    private final CarReturnedAclHandler carReturnedAclHandler;
    private final RentalCancelledAclHandler rentalCancelledAclHandler;

    public InternalIntegrationEventController(
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

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        switch (root.path("eventType").asText()) {
            case "ReservationCreated" -> reservationCreatedAclHandler.handle(toReservation(root));
            case "CarRented" -> carRentedAclHandler.handle(toCarRented(root));
            case "CarReturned" -> carReturnedAclHandler.handle(toCarReturned(root));
            case "RentalCancelled" -> rentalCancelledAclHandler.handle(toCancelled(root));
            default -> { }
        }
        return ResponseEntity.accepted().build();
    }

    private ReservationCreatedEvent toReservation(JsonNode root) {
        return new ReservationCreatedEvent(
                root.path("rentalId").asText(),
                root.path("vehicleId").asText(),
                root.path("customerId").asText(),
                root.path("periodStart").asText(),
                root.path("periodEnd").asText());
    }

    private CarRentedEvent toCarRented(JsonNode root) {
        return new CarRentedEvent(
                root.path("rentalId").asText(),
                root.path("vehicleId").asText(),
                root.path("customerId").asText(),
                root.path("actualStartDate").asText());
    }

    private CarReturnedEvent toCarReturned(JsonNode root) {
        return new CarReturnedEvent(
                root.path("rentalId").asText(),
                root.path("vehicleId").asText(),
                root.path("customerId").asText(),
                root.path("returnDate").asText(),
                root.path("periodStart").asText(null),
                root.path("vehicleCategory").asText(null),
                root.path("finalCostMinorUnits").asLong(0),
                root.path("currency").asText("PLN"));
    }

    private RentalCancelledEvent toCancelled(JsonNode root) {
        return new RentalCancelledEvent(root.path("rentalId").asText(), root.path("vehicleId").asText());
    }
}
