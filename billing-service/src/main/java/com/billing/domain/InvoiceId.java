package com.billing.domain;

import java.util.Objects;
import java.util.UUID;

public final class InvoiceId {

    private final String value;

    private InvoiceId(String value) {
        this.value = value;
    }

    public static InvoiceId generate() {
        return new InvoiceId(UUID.randomUUID().toString());
    }

    public static InvoiceId of(String value) {
        Objects.requireNonNull(value, "invoiceId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("invoiceId must not be blank");
        }
        return new InvoiceId(value);
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
        InvoiceId invoiceId = (InvoiceId) o;
        return value.equals(invoiceId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
