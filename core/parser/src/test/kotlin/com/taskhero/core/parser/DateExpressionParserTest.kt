package com.taskhero.core.parser

import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for DateExpressionParser.
 *
 * Tests:
 * - ISO dates (2025-12-31)
 * - Relative dates (+3d, -1w, +2m, +1y)
 * - Named dates (today, tomorrow, eom, eoy, etc.)
 * - Day of week (monday, tuesday, etc.)
 * - Invalid inputs return null
 * - Edge cases
 */
class DateExpressionParserTest {

    private val referenceDate = LocalDateTime(2025, 6, 15, 12, 0, 0)

    @Test
    fun `test ISO date format parses correctly`() {
        // When
        val result = DateExpressionParser.parse("2025-12-31", referenceDate)

        // Then
        assertNotNull("Should parse ISO date", result)
        assertEquals("Year should be 2025", 2025, result?.year)
        assertEquals("Month should be December", 12, result?.monthNumber)
        assertEquals("Day should be 31", 31, result?.dayOfMonth)
        assertEquals("Hour should be 0", 0, result?.hour)
        assertEquals("Minute should be 0", 0, result?.minute)
    }

    @Test
    fun `test ISO date with different months`() {
        // When
        val result = DateExpressionParser.parse("2024-02-14", referenceDate)

        // Then
        assertNotNull("Should parse ISO date", result)
        assertEquals("Year should be 2024", 2024, result?.year)
        assertEquals("Month should be February", 2, result?.monthNumber)
        assertEquals("Day should be 14", 14, result?.dayOfMonth)
    }

    @Test
    fun `test relative date plus days`() {
        // When
        val result = DateExpressionParser.parse("+3d", referenceDate)

        // Then
        assertNotNull("Should parse +3d", result)
        assertEquals("Should be 3 days in future", 18, result?.dayOfMonth)
        assertEquals("Month should remain June", 6, result?.monthNumber)
    }

    @Test
    fun `test relative date minus days`() {
        // When
        val result = DateExpressionParser.parse("-5d", referenceDate)

        // Then
        assertNotNull("Should parse -5d", result)
        assertEquals("Should be 5 days in past", 10, result?.dayOfMonth)
        assertEquals("Month should remain June", 6, result?.monthNumber)
    }

    @Test
    fun `test relative date plus weeks`() {
        // When
        val result = DateExpressionParser.parse("+2w", referenceDate)

        // Then
        assertNotNull("Should parse +2w", result)
        assertEquals("Should be 14 days (2 weeks) in future", 29, result?.dayOfMonth)
    }

    @Test
    fun `test relative date minus weeks`() {
        // When
        val result = DateExpressionParser.parse("-1w", referenceDate)

        // Then
        assertNotNull("Should parse -1w", result)
        assertEquals("Should be 7 days in past", 8, result?.dayOfMonth)
        assertEquals("Month should remain June", 6, result?.monthNumber)
    }

    @Test
    fun `test relative date plus months`() {
        // When
        val result = DateExpressionParser.parse("+2m", referenceDate)

        // Then
        assertNotNull("Should parse +2m", result)
        assertEquals("Month should be August", 8, result?.monthNumber)
        assertEquals("Day should remain 15", 15, result?.dayOfMonth)
    }

    @Test
    fun `test relative date minus months`() {
        // When
        val result = DateExpressionParser.parse("-3m", referenceDate)

        // Then
        assertNotNull("Should parse -3m", result)
        assertEquals("Month should be March", 3, result?.monthNumber)
        assertEquals("Day should remain 15", 15, result?.dayOfMonth)
    }

    @Test
    fun `test relative date plus years`() {
        // When
        val result = DateExpressionParser.parse("+1y", referenceDate)

        // Then
        assertNotNull("Should parse +1y", result)
        assertEquals("Year should be 2026", 2026, result?.year)
        assertEquals("Month should remain June", 6, result?.monthNumber)
        assertEquals("Day should remain 15", 15, result?.dayOfMonth)
    }

    @Test
    fun `test relative date minus years`() {
        // When
        val result = DateExpressionParser.parse("-2y", referenceDate)

        // Then
        assertNotNull("Should parse -2y", result)
        assertEquals("Year should be 2023", 2023, result?.year)
    }

    @Test
    fun `test named date today`() {
        // When
        val result = DateExpressionParser.parse("today", referenceDate)

        // Then
        assertNotNull("Should parse today", result)
        assertEquals("Year should match reference", 2025, result?.year)
        assertEquals("Month should match reference", 6, result?.monthNumber)
        assertEquals("Day should match reference", 15, result?.dayOfMonth)
        assertEquals("Hour should be 0", 0, result?.hour)
    }

    @Test
    fun `test named date tomorrow`() {
        // When
        val result = DateExpressionParser.parse("tomorrow", referenceDate)

        // Then
        assertNotNull("Should parse tomorrow", result)
        assertEquals("Day should be one more", 16, result?.dayOfMonth)
        assertEquals("Hour should be 0", 0, result?.hour)
    }

    @Test
    fun `test named date yesterday`() {
        // When
        val result = DateExpressionParser.parse("yesterday", referenceDate)

        // Then
        assertNotNull("Should parse yesterday", result)
        assertEquals("Day should be one less", 14, result?.dayOfMonth)
    }

    @Test
    fun `test named date eom (end of month)`() {
        // When
        val result = DateExpressionParser.parse("eom", referenceDate)

        // Then
        assertNotNull("Should parse eom", result)
        assertEquals("Should be last day of June", 30, result?.dayOfMonth)
        assertEquals("Month should be June", 6, result?.monthNumber)
        assertEquals("Hour should be 23", 23, result?.hour)
        assertEquals("Minute should be 59", 59, result?.minute)
    }

    @Test
    fun `test named date som (start of month)`() {
        // When
        val result = DateExpressionParser.parse("som", referenceDate)

        // Then
        assertNotNull("Should parse som", result)
        assertEquals("Should be first day of month", 1, result?.dayOfMonth)
        assertEquals("Month should be June", 6, result?.monthNumber)
        assertEquals("Hour should be 0", 0, result?.hour)
    }

    @Test
    fun `test named date eoy (end of year)`() {
        // When
        val result = DateExpressionParser.parse("eoy", referenceDate)

        // Then
        assertNotNull("Should parse eoy", result)
        assertEquals("Should be December", 12, result?.monthNumber)
        assertEquals("Should be 31st", 31, result?.dayOfMonth)
        assertEquals("Year should match reference", 2025, result?.year)
        assertEquals("Hour should be 23", 23, result?.hour)
    }

    @Test
    fun `test named date soy (start of year)`() {
        // When
        val result = DateExpressionParser.parse("soy", referenceDate)

        // Then
        assertNotNull("Should parse soy", result)
        assertEquals("Should be January", 1, result?.monthNumber)
        assertEquals("Should be 1st", 1, result?.dayOfMonth)
        assertEquals("Year should match reference", 2025, result?.year)
    }

    @Test
    fun `test day of week monday`() {
        // Given - reference is June 15, 2025 (Sunday)
        // When
        val result = DateExpressionParser.parse("monday", referenceDate)

        // Then
        assertNotNull("Should parse monday", result)
        assertEquals("Should be next Monday (June 16)", 16, result?.dayOfMonth)
    }

    @Test
    fun `test day of week tuesday`() {
        // Given - reference is June 15, 2025 (Sunday)
        // When
        val result = DateExpressionParser.parse("tuesday", referenceDate)

        // Then
        assertNotNull("Should parse tuesday", result)
        assertEquals("Should be next Tuesday (June 17)", 17, result?.dayOfMonth)
    }

    @Test
    fun `test day of week wednesday`() {
        // When
        val result = DateExpressionParser.parse("wednesday", referenceDate)

        // Then
        assertNotNull("Should parse wednesday", result)
        assertEquals("Should be next Wednesday (June 18)", 18, result?.dayOfMonth)
    }

    @Test
    fun `test day of week returns next occurrence not today`() {
        // Given - June 15, 2025 is a Sunday
        // When
        val result = DateExpressionParser.parse("sunday", referenceDate)

        // Then
        assertNotNull("Should parse sunday", result)
        assertEquals("Should be next Sunday (June 22, not today)", 22, result?.dayOfMonth)
    }

    @Test
    fun `test invalid date format returns null`() {
        // When
        val result = DateExpressionParser.parse("invalid-date", referenceDate)

        // Then
        assertNull("Invalid date should return null", result)
    }

    @Test
    fun `test malformed ISO date returns null`() {
        // When
        val result = DateExpressionParser.parse("2025-13-45", referenceDate)

        // Then
        assertNull("Malformed ISO date should return null", result)
    }

    @Test
    fun `test invalid relative format returns null`() {
        // When
        val result = DateExpressionParser.parse("+5x", referenceDate)

        // Then
        assertNull("Invalid relative format should return null", result)
    }

    @Test
    fun `test relative date without sign returns null`() {
        // When
        val result = DateExpressionParser.parse("5d", referenceDate)

        // Then
        assertNull("Relative date without sign should return null", result)
    }

    @Test
    fun `test empty string returns null`() {
        // When
        val result = DateExpressionParser.parse("", referenceDate)

        // Then
        assertNull("Empty string should return null", result)
    }

    @Test
    fun `test whitespace only returns null`() {
        // When
        val result = DateExpressionParser.parse("   ", referenceDate)

        // Then
        assertNull("Whitespace only should return null", result)
    }

    @Test
    fun `test parsing is case insensitive for named dates`() {
        // When
        val lower = DateExpressionParser.parse("today", referenceDate)
        val upper = DateExpressionParser.parse("TODAY", referenceDate)
        val mixed = DateExpressionParser.parse("ToDay", referenceDate)

        // Then
        assertNotNull("Lower case should parse", lower)
        assertNotNull("Upper case should parse", upper)
        assertNotNull("Mixed case should parse", mixed)
        assertEquals("All variations should be equal", lower, upper)
        assertEquals("All variations should be equal", lower, mixed)
    }

    @Test
    fun `test parsing is case insensitive for days of week`() {
        // When
        val lower = DateExpressionParser.parse("monday", referenceDate)
        val upper = DateExpressionParser.parse("MONDAY", referenceDate)
        val mixed = DateExpressionParser.parse("MoNdAy", referenceDate)

        // Then
        assertNotNull("Lower case should parse", lower)
        assertNotNull("Upper case should parse", upper)
        assertNotNull("Mixed case should parse", mixed)
        assertEquals("All variations should be equal", lower, upper)
    }

    @Test
    fun `test leading and trailing whitespace is trimmed`() {
        // When
        val result = DateExpressionParser.parse("  today  ", referenceDate)

        // Then
        assertNotNull("Should parse with whitespace", result)
        assertEquals("Day should match today", 15, result?.dayOfMonth)
    }

    @Test
    fun `test large relative values work`() {
        // When
        val result = DateExpressionParser.parse("+100d", referenceDate)

        // Then
        assertNotNull("Should parse large relative value", result)
        // 100 days from June 15 = September 23
        assertEquals("Should be in September", 9, result?.monthNumber)
        assertEquals("Should be 23rd", 23, result?.dayOfMonth)
    }

    @Test
    fun `test negative relative crossing year boundary`() {
        // When
        val result = DateExpressionParser.parse("-6m", referenceDate)

        // Then
        assertNotNull("Should parse crossing year boundary", result)
        assertEquals("Should be in December of previous year", 12, result?.monthNumber)
        assertEquals("Year should be 2024", 2024, result?.year)
    }

    @Test
    fun `test time is preserved for relative dates`() {
        // When
        val result = DateExpressionParser.parse("+1d", referenceDate)

        // Then
        assertNotNull("Should parse", result)
        assertEquals("Hour should be preserved", 12, result?.hour)
        assertEquals("Minute should be preserved", 0, result?.minute)
    }

    @Test
    fun `test time is reset to midnight for named dates`() {
        // When
        val result = DateExpressionParser.parse("today", referenceDate)

        // Then
        assertNotNull("Should parse", result)
        assertEquals("Hour should be 0", 0, result?.hour)
        assertEquals("Minute should be 0", 0, result?.minute)
    }

    @Test
    fun `test parse with default reference date uses current time`() {
        // When
        val result = DateExpressionParser.parse("today")

        // Then
        assertNotNull("Should parse with default reference", result)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        assertEquals("Should match current date", now.date, result?.date)
    }

    @Test
    fun `test all days of week parse correctly`() {
        // When
        val monday = DateExpressionParser.parse("monday", referenceDate)
        val tuesday = DateExpressionParser.parse("tuesday", referenceDate)
        val wednesday = DateExpressionParser.parse("wednesday", referenceDate)
        val thursday = DateExpressionParser.parse("thursday", referenceDate)
        val friday = DateExpressionParser.parse("friday", referenceDate)
        val saturday = DateExpressionParser.parse("saturday", referenceDate)
        val sunday = DateExpressionParser.parse("sunday", referenceDate)

        // Then
        assertNotNull("Monday should parse", monday)
        assertNotNull("Tuesday should parse", tuesday)
        assertNotNull("Wednesday should parse", wednesday)
        assertNotNull("Thursday should parse", thursday)
        assertNotNull("Friday should parse", friday)
        assertNotNull("Saturday should parse", saturday)
        assertNotNull("Sunday should parse", sunday)
    }

    @Test
    fun `test february end of month in leap year`() {
        // Given - February 2024 (leap year)
        val febRef = LocalDateTime(2024, 2, 15, 12, 0, 0)

        // When
        val result = DateExpressionParser.parse("eom", febRef)

        // Then
        assertNotNull("Should parse eom in February", result)
        assertEquals("Should be 29th in leap year", 29, result?.dayOfMonth)
        assertEquals("Should be February", 2, result?.monthNumber)
    }

    @Test
    fun `test february end of month in non-leap year`() {
        // Given - February 2025 (non-leap year)
        val febRef = LocalDateTime(2025, 2, 15, 12, 0, 0)

        // When
        val result = DateExpressionParser.parse("eom", febRef)

        // Then
        assertNotNull("Should parse eom in February", result)
        assertEquals("Should be 28th in non-leap year", 28, result?.dayOfMonth)
        assertEquals("Should be February", 2, result?.monthNumber)
    }
}
