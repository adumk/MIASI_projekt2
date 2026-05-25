package com.billing.domain;

import java.util.Objects;

public final class RentalCost {

    private final int rentalDays;
    private final long dailyRate;
    private final Money total;

    private RentalCost(int rentalDays, long dailyRate, Money total) {
        this.rentalDays = rentalDays;
        this.dailyRate = dailyRate;
        this.total = total;
    }

    public static RentalCost of(int rentalDays, long dailyRate, Money total) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("rentalDays must be positive");
        }
        if (dailyRate <= 0) {
            throw new IllegalArgumentException("dailyRate must be positive");
        }
        Objects.requireNonNull(total, "total must not be null");
        return new RentalCost(rentalDays, dailyRate, total);
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public long getDailyRate() {
        return dailyRate;
    }

    public Money getTotal() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RentalCost that = (RentalCost) o;
        return rentalDays == that.rentalDays
                && dailyRate == that.dailyRate
                && total.equals(that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rentalDays, dailyRate, total);
    }
}
