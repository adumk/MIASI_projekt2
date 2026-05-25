package com.billing.ports.out;

import com.billing.domain.DomainEvent;

public interface IDomainEventPublisher {

    void publish(DomainEvent event);
}
