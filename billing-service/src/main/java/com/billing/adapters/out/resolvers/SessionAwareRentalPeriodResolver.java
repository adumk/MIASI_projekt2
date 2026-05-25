package com.billing.adapters.out.resolvers;

import com.billing.domain.RentalId;
import com.billing.ports.out.IRentalBillingSessionStore;
import com.billing.ports.out.IRentalPeriodResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Primary
public class SessionAwareRentalPeriodResolver implements IRentalPeriodResolver {

    private final IRentalBillingSessionStore sessionStore;
    private final HardcodedRentalPeriodResolver fallback;

    public SessionAwareRentalPeriodResolver(
            IRentalBillingSessionStore sessionStore, HardcodedRentalPeriodResolver fallback) {
        this.sessionStore = sessionStore;
        this.fallback = fallback;
    }

    @Override
    public LocalDate resolveStartDate(String rentalId, LocalDate returnDate) {
        return sessionStore
                .findStartDate(RentalId.of(rentalId))
                .orElseGet(() -> fallback.resolveStartDate(rentalId, returnDate));
    }
}
