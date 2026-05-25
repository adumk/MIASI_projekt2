package com.billing.adapters.in.kafka;

import org.springframework.stereotype.Component;

@Component
public class IntegrationEventDispatcher {

    private final CarReturnedKafkaListener carReturnedKafkaListener;
    private final CarRentedKafkaListener carRentedKafkaListener;
    private final DamageReportedKafkaListener damageReportedKafkaListener;
    private final CostCalculatedKafkaListener costCalculatedKafkaListener;
    public IntegrationEventDispatcher(
            CarReturnedKafkaListener carReturnedKafkaListener,
            CarRentedKafkaListener carRentedKafkaListener,
            DamageReportedKafkaListener damageReportedKafkaListener,
            CostCalculatedKafkaListener costCalculatedKafkaListener) {
        this.carReturnedKafkaListener = carReturnedKafkaListener;
        this.carRentedKafkaListener = carRentedKafkaListener;
        this.damageReportedKafkaListener = damageReportedKafkaListener;
        this.costCalculatedKafkaListener = costCalculatedKafkaListener;
    }

    public void dispatch(String payload) {
        if (payload.contains("\"CarReturned\"")) {
            carReturnedKafkaListener.onCarReturned(payload);
        } else if (payload.contains("\"CarRented\"")) {
            carRentedKafkaListener.onCarRented(payload);
        } else if (payload.contains("\"DamageReported\"")) {
            damageReportedKafkaListener.onDamageReported(payload);
        } else if (payload.contains("\"CostCalculated\"")) {
            costCalculatedKafkaListener.onCostCalculated(payload);
        }
    }
}
