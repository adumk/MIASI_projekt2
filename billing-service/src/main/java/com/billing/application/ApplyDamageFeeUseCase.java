package com.billing.application;

import com.billing.domain.DamageSeverity;
import com.billing.domain.TariffCalculator;
import com.billing.ports.out.IDamageFeeStore;

public class ApplyDamageFeeUseCase {

    private final IDamageFeeStore damageFeeStore;

    public ApplyDamageFeeUseCase(IDamageFeeStore damageFeeStore) {
        this.damageFeeStore = damageFeeStore;
    }

    public void handle(ApplyDamageFeeCommand command) {
        DamageSeverity severity = DamageSeverity.valueOf(command.severity());
        long fee = TariffCalculator.damageFee(severity);
        damageFeeStore.storePendingFee(command.vehicleId(), fee);
    }
}
