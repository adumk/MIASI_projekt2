package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CustomerId — value object contract")
class CustomerIdTest {

    @Test
    @DisplayName("should create CustomerId from a valid UUID")
    void shouldCreateCustomerIdFromValidValue() {
        UUID uuid = UUID.randomUUID();

        CustomerId customerId = CustomerId.of(uuid);

        assertThat(customerId).isNotNull();
        assertThat(customerId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("should reject null UUID")
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> CustomerId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should reject malformed string that is not a valid UUID when parsing")
    void shouldRejectMalformedValue() {
        assertThatThrownBy(() -> CustomerId.parse("not-a-valid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should consider two CustomerIds with the same UUID as equal")
    void shouldBeEqualForTheSameValue() {
        UUID uuid = UUID.randomUUID();

        CustomerId first  = CustomerId.of(uuid);
        CustomerId second = CustomerId.of(uuid);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("should have consistent hashCode for equal CustomerIds")
    void shouldHaveConsistentHashCode() {
        UUID uuid = UUID.randomUUID();

        CustomerId first  = CustomerId.of(uuid);
        CustomerId second = CustomerId.of(uuid);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("should expose a readable toString containing the UUID")
    void shouldExposeReadableToString() {
        UUID uuid = UUID.randomUUID();

        CustomerId customerId = CustomerId.of(uuid);
        String result = customerId.toString();

        assertThat(result).isNotBlank();
        assertThat(result).contains(uuid.toString());
    }
}