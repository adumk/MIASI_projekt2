package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerNotFoundException;
import com.customer.ports.out.ICustomerRepository;

public class GetCustomerUseCase {

    private final ICustomerRepository customerRepository;

    public GetCustomerUseCase(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer handle(GetCustomerQuery query) {
        Customer customer = customerRepository.findById(query.customerId());
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found: " + query.customerId().getValue());
        }
        return customer;
    }
}
