package com.billing.application;

import com.billing.domain.Money;
import com.billing.domain.RentalId;

public record ProcessPaymentCommand(RentalId rentalId, Money amount) {
}
