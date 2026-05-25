package com.billing.application;

import com.billing.domain.RentalId;

public record GenerateInvoiceCommand(RentalId rentalId) {
}
