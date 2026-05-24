package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RentalId — value object contract")
class RentalIdTest {

    @Test
    @DisplayName("should create RentalId from a valid UUID")
    void shouldCreateRentalIdFromValidValue() {
        UUID uuid = UUID.randomUUID();

        RentalId rentalId = RentalId.of(uuid);

        assertThat(rentalId).isNotNull();
        assertThat(rentalId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("should reject null UUID")
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> RentalId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should reject malformed string that is not a valid UUID when parsing")
    void shouldRejectMalformedValue() {
        assertThatThrownBy(() -> RentalId.parse("not-a-valid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should consider two RentalIds with the same UUID as equal")
    void shouldBeEqualForTheSameValue() {
        UUID uuid = UUID.randomUUID();

        RentalId first  = RentalId.of(uuid);
        RentalId second = RentalId.of(uuid);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("should have consistent hashCode for equal RentalIds")
    void shouldHaveConsistentHashCode() {
        UUID uuid = UUID.randomUUID();

        RentalId first  = RentalId.of(uuid);
        RentalId second = RentalId.of(uuid);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("should expose a readable toString containing the UUID")
    void shouldExposeReadableToString() {
        UUID uuid = UUID.randomUUID();

        RentalId rentalId = RentalId.of(uuid);
        String result = rentalId.toString();

        assertThat(result).isNotBlank();
        assertThat(result).contains(uuid.toString());
    }
}