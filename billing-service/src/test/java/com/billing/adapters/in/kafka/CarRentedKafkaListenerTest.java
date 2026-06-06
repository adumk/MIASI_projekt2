package com.billing.adapters.in.kafka;

import com.billing.domain.RentalId;
import com.billing.ports.out.IRentalBillingSessionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarRentedKafkaListener — billing session start on CarRented event")
class CarRentedKafkaListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IRentalBillingSessionStore sessionStore;

    private CarRentedKafkaListener listener;

    @BeforeEach
    void setUp() {
        listener = new CarRentedKafkaListener(objectMapper, sessionStore);
    }

    @Test
    @DisplayName("Should start billing session when CarRented event received")
    void shouldStartBillingSessionWhenCarRentedEventReceived() {
        // given
        String payload = """
                {
                  "eventType": "CarRented",
                  "rentalId": "rental-001",
                  "actualStartDate": "2026-06-01",
                  "vehicleId": "vehicle-001",
                  "customerId": "customer-001"
                }
                """;

        // when
        listener.onCarRented(payload);

        // then
        verify(sessionStore).startSession(
                eq(RentalId.of("rental-001")),
                eq(LocalDate.of(2026, 6, 1)));
    }

    @Test
    @DisplayName("Should ignore events with wrong eventType")
    void shouldIgnoreEventsWithWrongEventType() {
        // given
        String payload = """
                {
                  "eventType": "CarReturned",
                  "rentalId": "rental-001",
                  "actualStartDate": "2026-06-01",
                  "vehicleId": "vehicle-001",
                  "customerId": "customer-001"
                }
                """;

        // when
        listener.onCarRented(payload);

        // then
        verify(sessionStore, never()).startSession(any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when payload is malformed")
    void shouldThrowIllegalStateExceptionWhenPayloadIsMalformed() {
        // given
        String payload = "not valid json";

        // when + then
        assertThatThrownBy(() -> listener.onCarRented(payload))
                .isInstanceOf(IllegalStateException.class);
    }
}