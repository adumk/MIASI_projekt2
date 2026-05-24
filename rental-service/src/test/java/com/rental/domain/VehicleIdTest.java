package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VehicleId — value object contract")
class VehicleIdTest {

    @Test
    @DisplayName("should create VehicleId from a valid UUID")
    void shouldCreateVehicleIdFromValidValue() {
        UUID uuid = UUID.randomUUID();

        VehicleId vehicleId = VehicleId.of(uuid);

        assertThat(vehicleId).isNotNull();
        assertThat(vehicleId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("should reject null UUID")
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> VehicleId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should reject malformed string that is not a valid UUID when parsing")
    void shouldRejectMalformedValue() {
        assertThatThrownBy(() -> VehicleId.parse("not-a-valid-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should consider two VehicleIds with the same UUID as equal")
    void shouldBeEqualForTheSameValue() {
        UUID uuid = UUID.randomUUID();

        VehicleId first  = VehicleId.of(uuid);
        VehicleId second = VehicleId.of(uuid);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("should have consistent hashCode for equal VehicleIds")
    void shouldHaveConsistentHashCode() {
        UUID uuid = UUID.randomUUID();

        VehicleId first  = VehicleId.of(uuid);
        VehicleId second = VehicleId.of(uuid);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("should expose a readable toString containing the UUID")
    void shouldExposeReadableToString() {
        UUID uuid = UUID.randomUUID();

        VehicleId vehicleId = VehicleId.of(uuid);
        String result = vehicleId.toString();

        assertThat(result).isNotBlank();
        assertThat(result).contains(uuid.toString());
    }
}