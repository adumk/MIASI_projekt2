package com.rental.domain;

import java.util.Objects;
import java.util.UUID;

public final class RentalId {

    private final String value;

    private RentalId(String value) {
        this.value = Objects.requireNonNull(value, "rentalId must not be null");
    }

    public static RentalId of(String value) {
        return new RentalId(value);
    }

    public static RentalId generate() {
        return new RentalId(UUID.randomUUID().toString());
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
        RentalId rentalId = (RentalId) o;
        return value.equals(rentalId.value);
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
