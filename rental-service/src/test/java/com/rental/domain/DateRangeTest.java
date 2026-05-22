package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DateRange — value object validation and business behaviour")
class DateRangeTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate IN_5_DAYS = TODAY.plusDays(5);
    private static final LocalDate IN_10_DAYS = TODAY.plusDays(10);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);

    // =========================================================================
    // Creation and basic validation
    // =========================================================================

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("Should create a valid DateRange when start is before end")
        void shouldCreateValidDateRange() {
            // given
            LocalDate start = TODAY;
            LocalDate end = IN_5_DAYS;

            // when
            DateRange range = DateRange.of(start, end);

            // then
            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("Should throw InvalidPeriodException when start is after end")
        void shouldThrowExceptionWhenStartIsAfterEnd() {
            // given
            LocalDate start = IN_5_DAYS;
            LocalDate end = TODAY;

            // when + then
            assertThatThrownBy(() -> DateRange.of(start, end))
                    .isInstanceOf(InvalidPeriodException.class)
                    .hasMessageContaining("start");
        }

        @Test
        @DisplayName("Should throw InvalidPeriodException when start date is in the past")
        void shouldThrowExceptionWhenDateInPast() {
            // given
            LocalDate pastStart = YESTERDAY;
            LocalDate end = IN_5_DAYS;

            // when + then
            assertThatThrownBy(() -> DateRange.of(pastStart, end))
                    .isInstanceOf(InvalidPeriodException.class)
                    .hasMessageContaining("past");
        }
    }

    // =========================================================================
    // Overlap detection — four canonical cases
    // =========================================================================

    @Nested
    @DisplayName("Overlap detection")
    class OverlapDetection {

        @Test
        @DisplayName("Should detect full overlap — two identical ranges")
        void shouldDetectFullOverlap() {
            // given
            DateRange first  = DateRange.of(TODAY, IN_10_DAYS);
            DateRange second = DateRange.of(TODAY, IN_10_DAYS);

            // when
            boolean result = first.overlapsWith(second);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should detect partial overlap — second range starts within first range")
        void shouldDetectPartialOverlap() {
            // given
            DateRange first  = DateRange.of(TODAY, IN_10_DAYS);
            DateRange second = DateRange.of(IN_5_DAYS, IN_10_DAYS.plusDays(3));

            // when
            boolean result = first.overlapsWith(second);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not detect overlap — ranges share only a boundary date (adjacent)")
        void shouldNotDetectOverlapForAdjacentRanges() {
            // given
            // first:  [TODAY ---- IN_5_DAYS]
            // second:                       [IN_5_DAYS ---- IN_10_DAYS]
            DateRange first  = DateRange.of(TODAY, IN_5_DAYS);
            DateRange second = DateRange.of(IN_5_DAYS, IN_10_DAYS);

            // when
            boolean result = first.overlapsWith(second);

            // then — touching boundaries are NOT considered an overlap
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should detect containment overlap — second range fully contained within first range")
        void shouldDetectContainmentOverlap() {
            // given
            // first:  [TODAY ---------------------- IN_10_DAYS]
            // second:         [IN_5_DAYS -- IN_5_DAYS+2]
            DateRange first  = DateRange.of(TODAY, IN_10_DAYS);
            DateRange second = DateRange.of(IN_5_DAYS, IN_5_DAYS.plusDays(2));

            // when
            boolean result = first.overlapsWith(second);

            // then
            assertThat(result).isTrue();
        }
    }
}