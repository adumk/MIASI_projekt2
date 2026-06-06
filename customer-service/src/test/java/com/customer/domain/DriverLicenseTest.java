package com.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DriverLicense — value object validation and expiry logic")
class DriverLicenseTest {

    @Test
    void shouldCreateValidDriverLicense() {
        DriverLicense license = DriverLicense.of("DL-12345", LocalDate.now().plusYears(1));

        assertThat(license.getNumber()).isEqualTo("DL-12345");
    }

    @Test
    void shouldTrimLicenseNumber() {
        DriverLicense license = DriverLicense.of("  DL-12345  ", LocalDate.now().plusYears(1));

        assertThat(license.getNumber()).isEqualTo("DL-12345");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldThrowExceptionForBlankLicenseNumber(String blank) {
        assertThatThrownBy(() -> DriverLicense.of(blank, LocalDate.now().plusYears(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForNullLicenseNumber() {
        assertThatThrownBy(() -> DriverLicense.of(null, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForNullExpiryDate() {
        assertThatThrownBy(() -> DriverLicense.of("DL-001", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void isValidOnShouldReturnTrueForFutureExpiryDate() {
        DriverLicense license = DriverLicense.of("DL-001", LocalDate.now().plusYears(1));

        assertThat(license.isValidOn(LocalDate.now())).isTrue();
    }

    @Test
    void isValidOnShouldReturnTrueOnExactExpiryDate() {
        DriverLicense license = DriverLicense.of("DL-001", LocalDate.now());

        assertThat(license.isValidOn(LocalDate.now())).isTrue();
    }

    @Test
    void isValidOnShouldReturnFalseForExpiredLicense() {
        DriverLicense license = DriverLicense.of("DL-001", LocalDate.now().minusDays(1));

        assertThat(license.isValidOn(LocalDate.now())).isFalse();
    }

    @Test
    void equalLicensesShouldBeEqual() {
        LocalDate expiry = LocalDate.now().plusYears(1);
        DriverLicense a = DriverLicense.of("DL-12345", expiry);
        DriverLicense b = DriverLicense.of("DL-12345", expiry);

        assertThat(a).isEqualTo(b);
    }
}