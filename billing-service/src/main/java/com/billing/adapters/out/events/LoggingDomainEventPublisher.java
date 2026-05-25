package com.billing.adapters.out.events;

import com.billing.domain.DomainEvent;
import com.billing.ports.out.IDomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
@ConditionalOnMissingBean(IDomainEventPublisher.class)
public class LoggingDomainEventPublisher implements IDomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event published: {}", event.getClass().getSimpleName());
    }
}
