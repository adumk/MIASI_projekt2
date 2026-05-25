package com.billing.adapters.out.events;

import com.billing.domain.DomainEvent;
import com.billing.ports.out.IDomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Log-only publisher used inside composite adapters (not a Spring bean). */
public class LoggingDomainEventPublisherDelegate implements IDomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisherDelegate.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event published: {}", event.getClass().getSimpleName());
    }
}
