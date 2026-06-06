package com.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email — value object validation and normalization")
class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        Email email = Email.of("jan.kowalski@example.com");

        assertThat(email.getValue()).isEqualTo("jan.kowalski@example.com");
    }

    @Test
    void shouldNormalizeEmailToLowercase() {
        Email email = Email.of("JAN.KOWALSKI@EXAMPLE.COM");

        assertThat(email.getValue()).isEqualTo("jan.kowalski@example.com");
    }

    @Test
    void shouldTrimWhitespace() {
        Email email = Email.of("  jan@example.com  ");

        assertThat(email.getValue()).isEqualTo("jan@example.com");
    }

    @Test
    void shouldThrowExceptionForNullEmail() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForBlankEmail() {
        assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForEmailWithoutAtSign() {
        assertThatThrownBy(() -> Email.of("notanemail"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForEmailWithoutDomain() {
        assertThatThrownBy(() -> Email.of("user@"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForEmailWithoutTLD() {
        assertThatThrownBy(() -> Email.of("user@domain"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalEmailsShouldBeEqual() {
        Email a = Email.of("jan@example.com");
        Email b = Email.of("jan@example.com");

        assertThat(a).isEqualTo(b);
    }

    @Test
    void emailNormalizedFromUppercaseShouldEqualLowercaseVersion() {
        Email upper = Email.of("JAN@EXAMPLE.COM");
        Email lower = Email.of("jan@example.com");

        assertThat(upper).isEqualTo(lower);
    }
}