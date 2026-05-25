package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.DomainEvent;
import com.customer.domain.DuplicateEmailException;
import com.customer.ports.out.ICustomerRepository;
import com.customer.ports.out.IEventPublisher;

public class RegisterCustomerUseCase {

    private final ICustomerRepository customerRepository;
    private final IEventPublisher eventPublisher;

    public RegisterCustomerUseCase(ICustomerRepository customerRepository, IEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    public CustomerId handle(RegisterCustomerCommand command) {
        if (customerRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException("Customer with this email already exists");
        }

        Customer customer = Customer.create(
                CustomerId.generate(),
                command.fullName(),
                command.email(),
                command.driverLicense());
        customer.register();

        customerRepository.save(customer);
        publishEvents(customer);
        return customer.getCustomerId();
    }

    private void publishEvents(Customer customer) {
        for (DomainEvent event : customer.getDomainEvents()) {
            eventPublisher.publish(event);
        }
        customer.clearDomainEvents();
    }
}
