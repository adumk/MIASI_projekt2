package com.billing.adapters.out.events;

import com.billing.domain.CustomerId;
import com.billing.domain.Money;
import com.billing.domain.RentalId;
import com.billing.ports.out.ICostCalculatedEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingCostCalculatedEventPublisher implements ICostCalculatedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingCostCalculatedEventPublisher.class);

    @Override
    public void publish(RentalId rentalId, CustomerId customerId, Money total) {
        log.info("CostCalculated (local) rental={} customer={} amount={} {}",
                rentalId.getValue(), customerId.getValue(), total.toMinorUnits(), total.getCurrency());
    }
}
