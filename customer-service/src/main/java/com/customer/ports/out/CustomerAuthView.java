package com.customer.ports.out;

import com.customer.domain.Customer;

public record CustomerAuthView(Customer customer, String passwordHash, String role) {}
