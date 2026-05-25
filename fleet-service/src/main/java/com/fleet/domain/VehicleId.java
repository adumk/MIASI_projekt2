package com.fleet.domain;

import java.util.Objects;
import java.util.UUID;

public final class VehicleId {

    private final String value;

    private VehicleId(String value) {
        this.value = Objects.requireNonNull(value, "vehicleId must not be null");
    }

    public static VehicleId of(String value) {
        return new VehicleId(value);
    }

    public static VehicleId generate() {
        return new VehicleId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VehicleId vehicleId = (VehicleId) o;
        return value.equals(vehicleId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
