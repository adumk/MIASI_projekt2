package com.rental.ports.out;

import com.rental.domain.DomainEvent;

public interface IEventPublisher {

    void publish(DomainEvent event);
}
