package com.billing.adapters.in.kafka;

import com.billing.application.CalculateCostUseCase;
import com.billing.domain.VehicleCategory;
import com.billing.ports.out.IRentalPeriodResolver;
import com.billing.ports.out.IVehicleCategoryResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarReturnedKafkaListener — cost calculation on CarReturned event")
class CarReturnedKafkaListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CalculateCostUseCase calculateCostUseCase;

    @Mock
    private IVehicleCategoryResolver vehicleCategoryResolver;

    @Mock
    private IRentalPeriodResolver rentalPeriodResolver;

    private CarReturnedKafkaListener listener;

    @BeforeEach
    void setUp() {
        listener = new CarReturnedKafkaListener(
                objectMapper, calculateCostUseCase, vehicleCategoryResolver, rentalPeriodResolver);
    }

    @Test
    @DisplayName("Should delegate to CalculateCostUseCase when CarReturned event received")
    void shouldDelegateToCalculateCostUseCaseWhenCarReturnedEventReceived() {
        // given
        when(vehicleCategoryResolver.resolve("vehicle-001")).thenReturn(VehicleCategory.STANDARD);

        String payload = """
                {
                  "eventType": "CarReturned",
                  "rentalId": "rental-001",
                  "customerId": "customer-001",
                  "vehicleId": "vehicle-001",
                  "periodStart": "2026-05-25",
                  "returnDate": "2026-06-01"
                }
                """;

        // when
        listener.onCarReturned(payload);

        // then
        verify(calculateCostUseCase).handle(argThat(cmd ->
                cmd.rentalId().getValue().equals("rental-001") &&
                        cmd.returnDate().equals(LocalDate.of(2026, 6, 1))));
    }

    @Test
    @DisplayName("Should resolve period start from store when missing in payload")
    void shouldResolvePeriodStartFromStoreWhenMissingInPayload() {
        // given
        when(vehicleCategoryResolver.resolve("vehicle-001")).thenReturn(VehicleCategory.STANDARD);
        when(rentalPeriodResolver.resolveStartDate(eq("rental-001"), any(LocalDate.class)))
                .thenReturn(LocalDate.of(2026, 5, 25));

        String payload = """
                {
                  "eventType": "CarReturned",
                  "rentalId": "rental-001",
                  "customerId": "customer-001",
                  "vehicleId": "vehicle-001",
                  "returnDate": "2026-06-01"
                }
                """;

        // when
        listener.onCarReturned(payload);

        // then
        verify(rentalPeriodResolver).resolveStartDate(eq("rental-001"), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should ignore events with wrong eventType")
    void shouldIgnoreEventsWithWrongEventType() {
        // given
        String payload = """
                {
                  "eventType": "CarRented",
                  "rentalId": "rental-001",
                  "vehicleId": "vehicle-001",
                  "returnDate": "2026-06-01"
                }
                """;

        // when
        listener.onCarReturned(payload);

        // then
        verify(calculateCostUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when returnDate is missing from payload")
    void shouldThrowWhenReturnDateMissingFromPayload() {

        String payload = """
                {
                  "eventType": "CarReturned",
                  "rentalId": "rental-001",
                  "customerId": "customer-001",
                  "vehicleId": "vehicle-001"
                }
                """;

        // when + then
        assertThatThrownBy(() -> listener.onCarReturned(payload))
                .isInstanceOf(IllegalStateException.class);
    }
}