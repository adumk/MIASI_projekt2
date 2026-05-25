package com.fleet.ports.out;

import com.fleet.domain.DomainEvent;

public interface IEventPublisher {

    void publish(DomainEvent event);
}
