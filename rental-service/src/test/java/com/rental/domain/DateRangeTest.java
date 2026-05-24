package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DateRange — value object contract")
class DateRangeTest {

    // -------------------------------------------------------------------------
    // Existing tests — preserved without modification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create a valid date range")
    void shouldCreateValidDateRange() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(5);

        DateRange range = DateRange.of(start, end);

        assertThat(range.start()).isEqualTo(start);
        assertThat(range.end()).isEqualTo(end);
    }

    @Test
    @DisplayName("should throw exception when start is after end")
    void shouldThrowExceptionWhenStartIsAfterEnd() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end   = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> DateRange.of(start, end))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    @DisplayName("should throw exception when date is in the past")
    void shouldThrowExceptionWhenDateInPast() {
        LocalDate start = LocalDate.now().minusDays(3);
        LocalDate end   = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> DateRange.of(start, end))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    @DisplayName("should detect full overlap between two ranges")
    void shouldDetectFullOverlap() {
        LocalDate start = LocalDate.now().plusDays(1);
        DateRange range = DateRange.of(start, start.plusDays(10));
        DateRange other = DateRange.of(start.plusDays(2), start.plusDays(7));

        assertThat(range.overlapsWith(other)).isTrue();
    }

    @Test
    @DisplayName("should detect partial overlap between two ranges")
    void shouldDetectPartialOverlap() {
        LocalDate start = LocalDate.now().plusDays(1);
        DateRange range = DateRange.of(start, start.plusDays(5));
        DateRange other = DateRange.of(start.plusDays(3), start.plusDays(8));

        assertThat(range.overlapsWith(other)).isTrue();
    }

    @Test
    @DisplayName("should not detect overlap for adjacent ranges")
    void shouldNotDetectOverlapForAdjacentRanges() {
        LocalDate start = LocalDate.now().plusDays(1);
        DateRange range = DateRange.of(start, start.plusDays(3));
        DateRange other = DateRange.of(start.plusDays(3), start.plusDays(6));

        assertThat(range.overlapsWith(other)).isFalse();
    }

    @Test
    @DisplayName("should detect containment as overlap")
    void shouldDetectContainmentOverlap() {
        LocalDate start = LocalDate.now().plusDays(1);
        DateRange outer = DateRange.of(start, start.plusDays(10));
        DateRange inner = DateRange.of(start.plusDays(2), start.plusDays(4));

        assertThat(outer.overlapsWith(inner)).isTrue();
    }

    // -------------------------------------------------------------------------
    // New tests — added to complete the value object contract
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should reject null start date")
    void shouldRejectNullStartDate() {
        LocalDate end = LocalDate.now().plusDays(5);

        assertThatThrownBy(() -> DateRange.of(null, end))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should reject null end date")
    void shouldRejectNullEndDate() {
        LocalDate start = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> DateRange.of(start, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should implement value equality for identical ranges")
    void shouldImplementValueEquality() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(5);

        DateRange first  = DateRange.of(start, end);
        DateRange second = DateRange.of(start, end);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("should have consistent hashCode for equal ranges")
    void shouldHaveConsistentHashCode() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(5);

        DateRange first  = DateRange.of(start, end);
        DateRange second = DateRange.of(start, end);

        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("should expose a readable toString containing start and end dates")
    void shouldExposeReadableToString() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end   = LocalDate.now().plusDays(5);

        DateRange range  = DateRange.of(start, end);
        String result = range.toString();

        assertThat(result).isNotBlank();
        assertThat(result).contains(start.toString());
        assertThat(result).contains(end.toString());
    }
}