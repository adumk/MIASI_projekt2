package com.rental.adapters.in.web;

import com.rental.adapters.in.kafka.PaymentConfirmedKafkaListener;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequestMapping("/internal/integration-events")
public class InternalIntegrationEventController {

    private final PaymentConfirmedKafkaListener paymentConfirmedKafkaListener;

    public InternalIntegrationEventController(PaymentConfirmedKafkaListener paymentConfirmedKafkaListener) {
        this.paymentConfirmedKafkaListener = paymentConfirmedKafkaListener;
    }

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody String payload) {
        if (payload.contains("\"PaymentConfirmed\"")) {
            paymentConfirmedKafkaListener.onPaymentConfirmed(payload);
        }
        return ResponseEntity.accepted().build();
    }
}
