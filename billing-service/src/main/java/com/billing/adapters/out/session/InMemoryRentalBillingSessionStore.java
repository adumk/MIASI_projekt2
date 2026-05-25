package com.billing.adapters.out.session;

import com.billing.domain.RentalId;
import com.billing.ports.out.IRentalBillingSessionStore;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRentalBillingSessionStore implements IRentalBillingSessionStore {

    private final Map<String, LocalDate> sessions = new ConcurrentHashMap<>();

    @Override
    public void startSession(RentalId rentalId, LocalDate actualStartDate) {
        sessions.put(rentalId.getValue(), actualStartDate);
    }

    @Override
    public Optional<LocalDate> findStartDate(RentalId rentalId) {
        return Optional.ofNullable(sessions.get(rentalId.getValue()));
    }
}
