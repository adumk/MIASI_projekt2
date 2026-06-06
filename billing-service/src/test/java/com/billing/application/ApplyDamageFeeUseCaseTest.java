package com.billing.application;

import com.billing.ports.out.IDamageFeeStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplyDamageFeeUseCase — storing damage fees by severity")
class ApplyDamageFeeUseCaseTest {

    @Mock
    private IDamageFeeStore damageFeeStore;

    private ApplyDamageFeeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ApplyDamageFeeUseCase(damageFeeStore);
    }

    @Test
    @DisplayName("Should store MINOR damage fee of 5000")
    void shouldStoreMinorDamageFee() {
        // when
        useCase.handle(new ApplyDamageFeeCommand("vehicle-001", "MINOR"));

        // then
        verify(damageFeeStore).storePendingFee("vehicle-001", 5000L);
    }

    @Test
    @DisplayName("Should store MODERATE damage fee of 15000")
    void shouldStoreModrateDamageFee() {
        // when
        useCase.handle(new ApplyDamageFeeCommand("vehicle-001", "MODERATE"));

        // then
        verify(damageFeeStore).storePendingFee("vehicle-001", 15000L);
    }

    @Test
    @DisplayName("Should store SEVERE damage fee of 50000")
    void shouldStoreSevereDamageFee() {
        // when
        useCase.handle(new ApplyDamageFeeCommand("vehicle-001", "SEVERE"));

        // then
        verify(damageFeeStore).storePendingFee("vehicle-001", 50000L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when unknown severity is passed")
    void shouldThrowWhenUnknownSeverityPassed() {
        // when + then
        assertThatThrownBy(() -> useCase.handle(new ApplyDamageFeeCommand("vehicle-001", "CATASTROPHIC")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(damageFeeStore, never()).storePendingFee(any(), anyLong());
    }
}