package com.rental.domain;

import java.util.Objects;

public final class CustomerId {

    private final String value;

    private CustomerId(String value) {
        this.value = Objects.requireNonNull(value, "customerId must not be null");
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
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
        CustomerId that = (CustomerId) o;
        return value.equals(that.value);
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
