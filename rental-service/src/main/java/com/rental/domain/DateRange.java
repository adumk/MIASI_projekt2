package com.rental.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class DateRange {

    private final LocalDate start;
    private final LocalDate end;
    private final boolean allowPastStart;

    private DateRange(LocalDate start, LocalDate end, boolean allowPastStart) {
        this.start = start;
        this.end = end;
        this.allowPastStart = allowPastStart;
        validate();
    }

    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(start, end, false);
    }

    public static DateRange ofHistorical(LocalDate start, LocalDate end) {
        return new DateRange(start, end, true);
    }

    private void validate() {
        if (start == null || end == null) {
            throw new InvalidPeriodException("start and end dates must not be null");
        }
        if (!start.isBefore(end)) {
            throw new InvalidPeriodException("start date must be before end date");
        }
        if (!allowPastStart && start.isBefore(LocalDate.now())) {
            throw new InvalidPeriodException("start date must not be in the past");
        }
    }

    public boolean overlapsWith(DateRange other) {
        Objects.requireNonNull(other, "other range must not be null");
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DateRange dateRange = (DateRange) o;
        return start.equals(dateRange.start) && end.equals(dateRange.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
