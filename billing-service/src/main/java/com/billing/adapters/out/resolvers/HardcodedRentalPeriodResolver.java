package com.billing.adapters.out.resolvers;

import com.billing.ports.out.IRentalPeriodResolver;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class HardcodedRentalPeriodResolver implements IRentalPeriodResolver {

    private static final Map<String, Integer> DAYS_BY_RENTAL = Map.of(
            "rental-001", 7,
            "rental-002", 3);

    @Override
    public LocalDate resolveStartDate(String rentalId, LocalDate returnDate) {
        int days = DAYS_BY_RENTAL.getOrDefault(rentalId, 1);
        return returnDate.minusDays(days);
    }
}
