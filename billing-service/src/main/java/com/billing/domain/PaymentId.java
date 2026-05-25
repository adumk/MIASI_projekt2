package com.billing.domain;

import java.util.Objects;
import java.util.UUID;

public final class PaymentId {

    private final String value;

    private PaymentId(String value) {
        this.value = value;
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID().toString());
    }

    public static PaymentId of(String value) {
        Objects.requireNonNull(value, "paymentId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("paymentId must not be blank");
        }
        return new PaymentId(value);
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
        PaymentId paymentId = (PaymentId) o;
        return value.equals(paymentId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
