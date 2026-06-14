package com.billing.adapters.in.kafka;

import com.billing.application.ApplyDamageFeeUseCase;
import com.billing.ports.out.IDamageFeeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DamageReportedKafkaListener — damage fee storage on DamageReported event")
class DamageReportedKafkaListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IDamageFeeStore damageFeeStore;

    private DamageReportedKafkaListener listener;

    @BeforeEach
    void setUp() {
        ApplyDamageFeeUseCase applyDamageFeeUseCase = new ApplyDamageFeeUseCase(damageFeeStore);
        listener = new DamageReportedKafkaListener(objectMapper, applyDamageFeeUseCase);
    }

    @Test
    @Disabled
    @DisplayName("Should store MINOR damage fee when DamageReported event received")
    void shouldStoreMinorDamageFeeWhenEventReceived() {
        // given
        String payload = """
                {
                  "eventType": "DamageReported",
                  "vehicleId": "vehicle-001",
                  "description": "Scratch on door",
                  "severity": "MINOR"
                }
                """;

        // when
        listener.onDamageReported(payload);

        // then
        verify(damageFeeStore).storePendingFee("vehicle-001", 5000L);
    }

    @Test
    @Disabled
    @DisplayName("Should store SEVERE damage fee when DamageReported event received")
    void shouldStoreSevereDamageFeeWhenEventReceived() {
        // given
        String payload = """
                {
                  "eventType": "DamageReported",
                  "vehicleId": "vehicle-002",
                  "description": "Major accident",
                  "severity": "SEVERE"
                }
                """;

        // when
        listener.onDamageReported(payload);

        // then
        verify(damageFeeStore).storePendingFee("vehicle-002", 50000L);
    }

    @Test
    @DisplayName("Should ignore events with wrong eventType")
    void shouldIgnoreEventsWithWrongEventType() {
        // given
        String payload = """
                {
                  "eventType": "CarReturned",
                  "vehicleId": "vehicle-001",
                  "severity": "MINOR"
                }
                """;

        // when
        listener.onDamageReported(payload);

        // then
        verify(damageFeeStore, never()).storePendingFee(any(), anyLong());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when payload is malformed")
    void shouldThrowIllegalStateExceptionWhenPayloadIsMalformed() {
        // given
        String payload = "not valid json";

        // when + then
        assertThatThrownBy(() -> listener.onDamageReported(payload))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when severity is unknown")
    void shouldThrowWhenSeverityIsUnknown() {
        // given
        String payload = """
                {
                  "eventType": "DamageReported",
                  "vehicleId": "vehicle-001",
                  "description": "Unknown damage",
                  "severity": "CATASTROPHIC"
                }
                """;

        // when + then
        assertThatThrownBy(() -> listener.onDamageReported(payload))
                .isInstanceOf(IllegalStateException.class);
    }
}