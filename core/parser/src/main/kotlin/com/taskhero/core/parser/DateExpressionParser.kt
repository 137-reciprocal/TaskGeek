package com.taskhero.core.parser

import kotlinx.datetime.*

/**
 * Parses date expressions into LocalDateTime instances.
 *
 * Supported patterns:
 * - ISO dates: "2025-12-31"
 * - Relative: "+3d", "-1w", "+2m", "+1y" (days, weeks, months, years)
 * - Named: "today", "tomorrow", "yesterday", "eom", "eoy", "soy", "som"
 * - Day of week: "monday", "tuesday", etc. (next occurrence)
 */
object DateExpressionParser {

    /**
     * Parse a date expression into a LocalDateTime.
     *
     * @param expr The date expression to parse
     * @param ref The reference date/time to use for relative expressions (defaults to now)
     * @return LocalDateTime if parsing succeeds, null otherwise
     */
    fun parse(
        expr: String,
        ref: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    ): LocalDateTime? {
        val trimmed = expr.trim().lowercase()

        return when {
            // ISO date format
            trimmed.matches(Regex("""\d{4}-\d{2}-\d{2}""")) -> parseIsoDate(trimmed)

            // Relative dates
            trimmed.matches(Regex("""[+-]\d+[dwmy]""")) -> parseRelativeDate(trimmed, ref)

            // Named dates
            trimmed in setOf("today", "tomorrow", "yesterday", "eom", "eoy", "soy", "som") ->
                parseNamedDate(trimmed, ref)

            // Day of week
            trimmed in setOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday") ->
                parseDayOfWeek(trimmed, ref)

            else -> null
        }
    }

    private fun parseIsoDate(expr: String): LocalDateTime? {
        return try {
            val date = LocalDate.parse(expr)
            date.atTime(0, 0)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseRelativeDate(expr: String, ref: LocalDateTime): LocalDateTime? {
        val sign = if (expr.startsWith('+')) 1 else -1
        val amount = expr.substring(1, expr.length - 1).toIntOrNull() ?: return null
        val unit = expr.last()

        val value = sign * amount

        return when (unit) {
            'd' -> ref.date.plus(value, DateTimeUnit.DAY).atTime(ref.time)
            'w' -> ref.date.plus(value * 7, DateTimeUnit.DAY).atTime(ref.time)
            'm' -> ref.date.plus(value, DateTimeUnit.MONTH).atTime(ref.time)
            'y' -> ref.date.plus(value, DateTimeUnit.YEAR).atTime(ref.time)
            else -> null
        }
    }

    private fun parseNamedDate(expr: String, ref: LocalDateTime): LocalDateTime {
        val date = ref.date

        return when (expr) {
            "today" -> ref.date.atTime(0, 0)
            "tomorrow" -> date.plus(1, DateTimeUnit.DAY).atTime(0, 0)
            "yesterday" -> date.plus(-1, DateTimeUnit.DAY).atTime(0, 0)
            "som" -> LocalDate(date.year, date.month, 1).atTime(0, 0) // Start of month
            "eom" -> { // End of month
                val nextMonth = date.plus(1, DateTimeUnit.MONTH)
                val firstOfNextMonth = LocalDate(nextMonth.year, nextMonth.month, 1)
                firstOfNextMonth.plus(-1, DateTimeUnit.DAY).atTime(23, 59, 59)
            }
            "soy" -> LocalDate(date.year, 1, 1).atTime(0, 0) // Start of year
            "eoy" -> LocalDate(date.year, 12, 31).atTime(23, 59, 59) // End of year
            else -> ref
        }
    }

    private fun parseDayOfWeek(expr: String, ref: LocalDateTime): LocalDateTime {
        val targetDay = when (expr) {
            "monday" -> DayOfWeek.MONDAY
            "tuesday" -> DayOfWeek.TUESDAY
            "wednesday" -> DayOfWeek.WEDNESDAY
            "thursday" -> DayOfWeek.THURSDAY
            "friday" -> DayOfWeek.FRIDAY
            "saturday" -> DayOfWeek.SATURDAY
            "sunday" -> DayOfWeek.SUNDAY
            else -> return ref
        }

        val currentDay = ref.date.dayOfWeek
        val daysUntilTarget = ((targetDay.ordinal - currentDay.ordinal + 7) % 7).let {
            if (it == 0) 7 else it // Next occurrence, not today
        }

        return ref.date.plus(daysUntilTarget, DateTimeUnit.DAY).atTime(0, 0)
    }
}
