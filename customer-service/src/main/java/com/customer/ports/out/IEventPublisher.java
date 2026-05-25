package com.customer.ports.out;

import com.customer.domain.DomainEvent;

public interface IEventPublisher {

    void publish(DomainEvent event);
}
