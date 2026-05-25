package com.billing.adapters.in.kafka;

import com.rental.events.EventTopics;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka endpoints active only when not using local profile (Docker / integration). */
@Component
@Profile("!local")
public class BillingKafkaConsumers {

    private final CarReturnedKafkaListener carReturnedKafkaListener;
    private final CarRentedKafkaListener carRentedKafkaListener;
    private final DamageReportedKafkaListener damageReportedKafkaListener;
    private final CostCalculatedKafkaListener costCalculatedKafkaListener;
    private final RentalCancelledKafkaListener rentalCancelledKafkaListener;

    public BillingKafkaConsumers(
            CarReturnedKafkaListener carReturnedKafkaListener,
            CarRentedKafkaListener carRentedKafkaListener,
            DamageReportedKafkaListener damageReportedKafkaListener,
            CostCalculatedKafkaListener costCalculatedKafkaListener,
            RentalCancelledKafkaListener rentalCancelledKafkaListener) {
        this.carReturnedKafkaListener = carReturnedKafkaListener;
        this.carRentedKafkaListener = carRentedKafkaListener;
        this.damageReportedKafkaListener = damageReportedKafkaListener;
        this.costCalculatedKafkaListener = costCalculatedKafkaListener;
        this.rentalCancelledKafkaListener = rentalCancelledKafkaListener;
    }

    @KafkaListener(topics = EventTopics.CAR_EVENTS, groupId = "billing-service")
    public void onCarReturned(String payload) {
        carReturnedKafkaListener.onCarReturned(payload);
    }

    @KafkaListener(topics = EventTopics.CAR_EVENTS, groupId = "billing-service-rented")
    public void onCarRented(String payload) {
        carRentedKafkaListener.onCarRented(payload);
    }

    @KafkaListener(topics = EventTopics.CAR_EVENTS, groupId = "billing-service-damage")
    public void onDamageReported(String payload) {
        damageReportedKafkaListener.onDamageReported(payload);
    }

    @KafkaListener(topics = EventTopics.BILLING_EVENTS, groupId = "billing-service-invoice")
    public void onCostCalculated(String payload) {
        costCalculatedKafkaListener.onCostCalculated(payload);
    }

    @KafkaListener(topics = EventTopics.CAR_EVENTS, groupId = "billing-service-cancelled")
    public void onRentalCancelled(String payload) {
        rentalCancelledKafkaListener.onRentalCancelled(payload);
    }
}
