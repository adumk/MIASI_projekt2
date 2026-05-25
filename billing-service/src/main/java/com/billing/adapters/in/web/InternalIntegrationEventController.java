package com.billing.adapters.in.web;

import com.billing.adapters.in.kafka.IntegrationEventDispatcher;
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

    private final IntegrationEventDispatcher dispatcher;

    public InternalIntegrationEventController(IntegrationEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody String payload) {
        try {
            dispatcher.dispatch(payload);
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(InternalIntegrationEventController.class)
                    .error("Integration event processing failed", ex);
        }
        return ResponseEntity.accepted().build();
    }
}
