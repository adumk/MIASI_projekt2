package com.billing.ports.out;

public interface IDamageFeeStore {

    void storePendingFee(String vehicleId, long feeMinorUnits);

    long consumePendingFee(String vehicleId);
}
