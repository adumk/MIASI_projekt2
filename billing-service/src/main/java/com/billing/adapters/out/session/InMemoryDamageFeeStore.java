package com.billing.adapters.out.session;

import com.billing.ports.out.IDamageFeeStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryDamageFeeStore implements IDamageFeeStore {

    private final Map<String, Long> fees = new ConcurrentHashMap<>();

    @Override
    public void storePendingFee(String vehicleId, long feeMinorUnits) {
        fees.put(vehicleId, feeMinorUnits);
    }

    @Override
    public long consumePendingFee(String vehicleId) {
        Long fee = fees.remove(vehicleId);
        return fee != null ? fee : 0L;
    }
}
