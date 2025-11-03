package com.taskhero.core.parser

import kotlinx.datetime.DateTimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Parses ISO-8601 duration strings for task recurrence.
 *
 * Supported formats:
 * - P1D (1 day, daily)
 * - P1W (1 week, weekly)
 * - P1M (1 month, monthly)
 * - P1Y (1 year, yearly)
 */
object RecurrenceParser {

    /**
     * Parse an ISO-8601 duration string.
     *
     * @param recurrence The duration string to parse (e.g., "P1D", "P1W", "P1M", "P1Y")
     * @return Duration if parsing succeeds, null otherwise
     */
    fun parse(recurrence: String): Duration? {
        val trimmed = recurrence.trim().uppercase()

        if (!trimmed.startsWith("P")) {
            return null
        }

        return try {
            when {
                // Daily: P1D, P2D, etc.
                trimmed.matches(Regex("""P\d+D""")) -> {
                    val days = trimmed.substring(1, trimmed.length - 1).toInt()
                    Duration.days(days)
                }

                // Weekly: P1W, P2W, etc.
                trimmed.matches(Regex("""P\d+W""")) -> {
                    val weeks = trimmed.substring(1, trimmed.length - 1).toInt()
                    Duration.days(weeks * 7)
                }

                // Monthly: P1M, P2M, etc.
                // Approximate to 30 days for Duration representation
                trimmed.matches(Regex("""P\d+M""")) -> {
                    val months = trimmed.substring(1, trimmed.length - 1).toInt()
                    Duration.days(months * 30)
                }

                // Yearly: P1Y, P2Y, etc.
                // Approximate to 365 days for Duration representation
                trimmed.matches(Regex("""P\d+Y""")) -> {
                    val years = trimmed.substring(1, trimmed.length - 1).toInt()
                    Duration.days(years * 365)
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse recurrence string and return the unit type and amount.
     * Useful for more precise date calculations using DateTimeUnit.
     *
     * @param recurrence The duration string to parse
     * @return Pair of (amount, unit) if parsing succeeds, null otherwise
     */
    fun parseToDateTimeUnit(recurrence: String): Pair<Int, RecurrenceUnit>? {
        val trimmed = recurrence.trim().uppercase()

        if (!trimmed.startsWith("P")) {
            return null
        }

        return try {
            when {
                trimmed.matches(Regex("""P\d+D""")) -> {
                    val days = trimmed.substring(1, trimmed.length - 1).toInt()
                    days to RecurrenceUnit.DAY
                }

                trimmed.matches(Regex("""P\d+W""")) -> {
                    val weeks = trimmed.substring(1, trimmed.length - 1).toInt()
                    weeks to RecurrenceUnit.WEEK
                }

                trimmed.matches(Regex("""P\d+M""")) -> {
                    val months = trimmed.substring(1, trimmed.length - 1).toInt()
                    months to RecurrenceUnit.MONTH
                }

                trimmed.matches(Regex("""P\d+Y""")) -> {
                    val years = trimmed.substring(1, trimmed.length - 1).toInt()
                    years to RecurrenceUnit.YEAR
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    enum class RecurrenceUnit {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }
}
