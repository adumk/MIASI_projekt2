package com.billing.ports.out;

import com.billing.domain.CustomerId;
import com.billing.domain.Money;
import com.billing.domain.RentalId;

public interface ICostCalculatedEventPublisher {

    void publish(RentalId rentalId, CustomerId customerId, Money total);
}
