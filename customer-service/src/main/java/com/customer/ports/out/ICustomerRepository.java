package com.customer.ports.out;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.Email;

public interface ICustomerRepository {

    void save(Customer customer);

    Customer findById(CustomerId customerId);

    boolean existsByEmail(Email email);

    Customer findByEmail(Email email);

    CustomerAuthView findAuthByEmail(Email email);

    void saveWithPassword(Customer customer, String passwordHash, String role);
}
