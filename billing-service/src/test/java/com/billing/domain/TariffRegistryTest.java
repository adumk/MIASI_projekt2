package com.billing.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TariffRegistry — daily rates for all vehicle categories")
class TariffRegistryTest {

    @AfterEach
    void restoreEconomyRate() {
        TariffRegistry.setDailyRate(VehicleCategory.ECONOMY, 100L);
    }

    @Test
    void shouldReturnExpectedRateForEconomy() {
        assertThat(TariffRegistry.getDailyRate(VehicleCategory.ECONOMY)).isEqualTo(100L);
    }

    @Test
    void shouldReturnExpectedRateForStandard() {
        assertThat(TariffRegistry.getDailyRate(VehicleCategory.STANDARD)).isEqualTo(150L);
    }

    @Test
    void shouldReturnExpectedRateForPremium() {
        assertThat(TariffRegistry.getDailyRate(VehicleCategory.PREMIUM)).isEqualTo(250L);
    }

    @Test
    void shouldReturnExpectedRateForSuv() {
        assertThat(TariffRegistry.getDailyRate(VehicleCategory.SUV)).isEqualTo(300L);
    }

    @Test
    void shouldReturnExpectedRateForVan() {
        assertThat(TariffRegistry.getDailyRate(VehicleCategory.VAN)).isEqualTo(180L);
    }

    @Test
    void shouldAllowDynamicRateUpdate() {
        TariffRegistry.setDailyRate(VehicleCategory.ECONOMY, 120L);

        assertThat(TariffRegistry.getDailyRate(VehicleCategory.ECONOMY)).isEqualTo(120L);
    }

    @Test
    void shouldThrowExceptionWhenSettingZeroOrNegativeRate() {
        assertThatThrownBy(() -> TariffRegistry.setDailyRate(VehicleCategory.ECONOMY, 0L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> TariffRegistry.setDailyRate(VehicleCategory.ECONOMY, -50L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void snapshotShouldContainAllCategories() {
        var snapshot = TariffRegistry.snapshot();

        assertThat(snapshot.keySet()).containsExactlyInAnyOrder(
                "ECONOMY", "STANDARD", "PREMIUM", "SUV", "VAN"
        );
    }
}