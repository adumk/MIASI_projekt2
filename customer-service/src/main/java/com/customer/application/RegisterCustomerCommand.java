package com.customer.application;

import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.PersonName;

public record RegisterCustomerCommand(
        PersonName fullName, Email email, DriverLicense driverLicense) {}
