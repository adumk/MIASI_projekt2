package com.customer.adapters.out.events;

import com.customer.domain.DomainEvent;
import com.customer.ports.out.IEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventPublisher implements IEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event published: {}", event.getClass().getSimpleName());
    }
}
