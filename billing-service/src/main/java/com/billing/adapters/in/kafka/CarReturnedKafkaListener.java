package com.billing.adapters.in.kafka;

import com.billing.application.CalculateCostCommand;
import com.billing.application.CalculateCostUseCase;
import com.billing.application.GenerateInvoiceCommand;
import com.billing.application.GenerateInvoiceUseCase;
import com.billing.domain.CustomerId;
import com.billing.domain.RentalId;
import com.billing.ports.out.IRentalPeriodResolver;
import com.billing.ports.out.IVehicleCategoryResolver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CarReturnedKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(CarReturnedKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final CalculateCostUseCase calculateCostUseCase;
    private final IVehicleCategoryResolver vehicleCategoryResolver;
    private final IRentalPeriodResolver rentalPeriodResolver;

    public CarReturnedKafkaListener(
            ObjectMapper objectMapper,
            CalculateCostUseCase calculateCostUseCase,
            IVehicleCategoryResolver vehicleCategoryResolver,
            IRentalPeriodResolver rentalPeriodResolver) {
        this.objectMapper = objectMapper;
        this.calculateCostUseCase = calculateCostUseCase;
        this.vehicleCategoryResolver = vehicleCategoryResolver;
        this.rentalPeriodResolver = rentalPeriodResolver;
    }

    public void onCarReturned(String payload) {
        try {
            JsonNode root = IntegrationEventJson.read(objectMapper, payload);
            if (!"CarReturned".equals(IntegrationEventJson.text(root, "eventType"))) {
                return;
            }

            String returnDateRaw = IntegrationEventJson.text(root, "returnDate");
            if (returnDateRaw == null || returnDateRaw.isBlank()) {
                throw new IllegalArgumentException("CarReturned missing returnDate");
            }
            LocalDate returnDate = LocalDate.parse(returnDateRaw);
            RentalId rentalId = RentalId.of(IntegrationEventJson.text(root, "rentalId"));
            CustomerId customerId = CustomerId.of(IntegrationEventJson.text(root, "customerId"));
            String vehicleId = IntegrationEventJson.text(root, "vehicleId");

            String periodStartRaw = IntegrationEventJson.text(root, "periodStart");
            LocalDate startDate = periodStartRaw != null && !periodStartRaw.isBlank()
                    ? LocalDate.parse(periodStartRaw)
                    : rentalPeriodResolver.resolveStartDate(rentalId.getValue(), returnDate);

            CalculateCostCommand costCommand = new CalculateCostCommand(
                    rentalId,
                    customerId,
                    vehicleId,
                    vehicleCategoryResolver.resolve(vehicleId),
                    startDate,
                    returnDate);

            calculateCostUseCase.handle(costCommand);

            log.info("Processed CarReturned for rental {}", rentalId.getValue());
        } catch (Exception e) {
            log.error("Failed to process CarReturned event", e);
            throw new IllegalStateException("Failed to process CarReturned event", e);
        }
    }
}
