package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerNotFoundException;
import com.customer.ports.out.ICustomerRepository;

public class BlockCustomerUseCase {

    private final ICustomerRepository customerRepository;

    public BlockCustomerUseCase(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void handle(BlockCustomerCommand command) {
        Customer customer = customerRepository.findById(command.customerId());
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found: " + command.customerId().getValue());
        }

        customer.block(command.reason());
        customerRepository.save(customer);
    }
}
