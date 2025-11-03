package com.taskhero.domain.report.model

import java.time.LocalDate

/**
 * Represents a date range for filtering reports.
 *
 * @property startDate The start date of the range (inclusive)
 * @property endDate The end date of the range (inclusive)
 */
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    companion object {
        /**
         * Creates a date range for the last N days from today.
         */
        fun lastNDays(days: Int): DateRange {
            val end = LocalDate.now()
            val start = end.minusDays(days.toLong() - 1)
            return DateRange(start, end)
        }

        /**
         * Creates a date range for the current week.
         */
        fun currentWeek(): DateRange {
            val today = LocalDate.now()
            val start = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val end = start.plusDays(6)
            return DateRange(start, end)
        }

        /**
         * Creates a date range for the current month.
         */
        fun currentMonth(): DateRange {
            val today = LocalDate.now()
            val start = today.withDayOfMonth(1)
            val end = today.withDayOfMonth(today.lengthOfMonth())
            return DateRange(start, end)
        }
    }
}
