package com.customer.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class DriverLicense {

    private final String number;
    private final LocalDate expiryDate;

    private DriverLicense(String number, LocalDate expiryDate) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("license number must not be blank");
        }
        this.number = number.trim();
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate must not be null");
    }

    public static DriverLicense of(String number, LocalDate expiryDate) {
        return new DriverLicense(number, expiryDate);
    }

    public boolean isValidOn(LocalDate date) {
        return !expiryDate.isBefore(date);
    }

    public String getNumber() {
        return number;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DriverLicense that = (DriverLicense) o;
        return number.equals(that.number) && expiryDate.equals(that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, expiryDate);
    }
}
