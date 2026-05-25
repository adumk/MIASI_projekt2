package com.rental.ports.out;

import com.rental.domain.Customer;
import com.rental.domain.CustomerId;

public interface ICustomerVerificationPort {

    Customer findEligibleCustomer(CustomerId customerId);
}
