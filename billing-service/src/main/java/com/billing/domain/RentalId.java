package com.billing.domain;

import java.util.Objects;

public final class RentalId {

    private final String value;

    private RentalId(String value) {
        this.value = value;
    }

    public static RentalId of(String value) {
        Objects.requireNonNull(value, "rentalId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("rentalId must not be blank");
        }
        return new RentalId(value);
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
        return Objects.hash(value);
    }
}
