package com.customer.application;

import com.customer.domain.CustomerId;

public record BlockCustomerCommand(CustomerId customerId, String reason) {}
