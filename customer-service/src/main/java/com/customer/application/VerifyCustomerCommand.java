package com.customer.application;

import com.customer.domain.CustomerId;

public record VerifyCustomerCommand(CustomerId customerId) {}
