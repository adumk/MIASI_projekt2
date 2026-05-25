package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerNotFoundException;
import com.customer.domain.DomainEvent;
import com.customer.ports.out.ICustomerRepository;
import com.customer.ports.out.IEventPublisher;

public class VerifyCustomerUseCase {

    private final ICustomerRepository customerRepository;
    private final IEventPublisher eventPublisher;

    public VerifyCustomerUseCase(ICustomerRepository customerRepository, IEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    public void handle(VerifyCustomerCommand command) {
        Customer customer = customerRepository.findById(command.customerId());
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found: " + command.customerId().getValue());
        }

        customer.verify();
        customerRepository.save(customer);
        publishEvents(customer);
    }

    private void publishEvents(Customer customer) {
        for (DomainEvent event : customer.getDomainEvents()) {
            eventPublisher.publish(event);
        }
        customer.clearDomainEvents();
    }
}
