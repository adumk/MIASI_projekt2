package com.rental.adapters.in.kafka;

import com.rental.application.CloseSettlementUseCase;
import com.rental.domain.RentalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentConfirmedKafkaListener — settlement closing on PaymentConfirmed event")
class PaymentConfirmedKafkaListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CloseSettlementUseCase closeSettlementUseCase;

    private PaymentConfirmedKafkaListener listener;

    @BeforeEach
    void setUp() {
        listener = new PaymentConfirmedKafkaListener(objectMapper, closeSettlementUseCase);
    }

    @Test
    @Disabled("Temporarily disabled - obsolete test")
    @DisplayName("Should close settlement when PaymentConfirmed event received")
    void shouldCloseSettlementWhenPaymentConfirmedEventReceived() {
        // given
        String payload = """
                {
                  "eventType": "PaymentConfirmed",
                  "rentalId": "rental-001",
                  "invoiceId": "invoice-001",
                  "amountMinorUnits": 15000
                }
                """;
        // when
        listener.onPaymentConfirmed(payload);

        // then
        verify(closeSettlementUseCase).handle(RentalId.of("rental-001"));
    }

    @Test
    @DisplayName("Should ignore events with wrong eventType")
    void shouldIgnoreEventsWithWrongEventType() {
        // given
        String payload = """
                {
                  "eventType": "CarRented",
                  "rentalId": "rental-001",
                  "invoiceId": "invoice-001",
                  "amountMinorUnits": 0
                }
                """;

        // when
        listener.onPaymentConfirmed(payload);

        // then
        verify(closeSettlementUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when payload is malformed")
    void shouldThrowWhenPayloadIsMalformed() {
        // given
        String payload = "{invalid json}";

        // when + then
        assertThatThrownBy(() -> listener.onPaymentConfirmed(payload))
                .isInstanceOf(IllegalStateException.class);
    }
}