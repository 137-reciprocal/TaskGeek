package com.taskhero.core.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for BrainDumpParser.
 */
class BrainDumpParserTest {

    @Test
    fun `parse returns empty list for blank input`() {
        val input = ""
        val result = BrainDumpParser.parse(input)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse returns empty list for whitespace-only input`() {
        val input = "   \n  \n  "
        val result = BrainDumpParser.parse(input)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse comma-separated tasks`() {
        val input = "Buy milk, Call dentist, Review code"
        val result = BrainDumpParser.parse(input)

        assertEquals(3, result.size)
        assertEquals("Buy milk", result[0].description)
        assertEquals("Call dentist", result[1].description)
        assertEquals("Review code", result[2].description)
    }

    @Test
    fun `parse newline-separated tasks`() {
        val input = """
            Buy milk
            Call dentist
            Review code
        """.trimIndent()
        val result = BrainDumpParser.parse(input)

        assertEquals(3, result.size)
        assertEquals("Buy milk", result[0].description)
        assertEquals("Call dentist", result[1].description)
        assertEquals("Review code", result[2].description)
    }

    @Test
    fun `parse mixed comma and newline separated tasks`() {
        val input = """
            Buy milk, Call dentist
            Review code, Write tests
        """.trimIndent()
        val result = BrainDumpParser.parse(input)

        assertEquals(4, result.size)
        assertEquals("Buy milk", result[0].description)
        assertEquals("Call dentist", result[1].description)
        assertEquals("Review code", result[2].description)
        assertEquals("Write tests", result[3].description)
    }

    @Test
    fun `parse tasks with natural language metadata`() {
        val input = "Buy milk tomorrow, Call dentist p1, Review code #work"
        val result = BrainDumpParser.parse(input)

        assertEquals(3, result.size)

        // First task with due date
        assertEquals("Buy milk", result[0].description)
        assertTrue(result[0].dueDate != null)

        // Second task with priority
        assertEquals("Call dentist", result[1].description)
        assertEquals("HIGH", result[1].priority?.name)

        // Third task with project
        assertEquals("Review code", result[2].description)
        assertEquals("work", result[2].project)
    }

    @Test
    fun `parse handles empty lines`() {
        val input = """
            Buy milk

            Call dentist

            Review code
        """.trimIndent()
        val result = BrainDumpParser.parse(input)

        assertEquals(3, result.size)
        assertEquals("Buy milk", result[0].description)
        assertEquals("Call dentist", result[1].description)
        assertEquals("Review code", result[2].description)
    }

    @Test
    fun `parse handles trailing commas`() {
        val input = "Buy milk, Call dentist, Review code,"
        val result = BrainDumpParser.parse(input)

        assertEquals(3, result.size)
        assertEquals("Buy milk", result[0].description)
        assertEquals("Call dentist", result[1].description)
        assertEquals("Review code", result[2].description)
    }

    @Test
    fun `parse handles double commas`() {
        val input = "Buy milk,, Call dentist, Review code"
        val result = BrainDumpParser.parse(input)

        assertEquals(3, result.size)
        assertEquals("Buy milk", result[0].description)
        assertEquals("Call dentist", result[1].description)
        assertEquals("Review code", result[2].description)
    }

    @Test
    fun `parse single task without commas uses newline mode`() {
        val input = "Buy milk tomorrow"
        val result = BrainDumpParser.parse(input)

        assertEquals(1, result.size)
        assertEquals("Buy milk", result[0].description)
        assertTrue(result[0].dueDate != null)
    }

    @Test
    fun `countTasks returns correct count for comma-separated`() {
        val input = "Buy milk, Call dentist, Review code"
        val count = BrainDumpParser.countTasks(input)
        assertEquals(3, count)
    }

    @Test
    fun `countTasks returns correct count for newline-separated`() {
        val input = """
            Buy milk
            Call dentist
            Review code
        """.trimIndent()
        val count = BrainDumpParser.countTasks(input)
        assertEquals(3, count)
    }

    @Test
    fun `countTasks returns zero for blank input`() {
        val input = ""
        val count = BrainDumpParser.countTasks(input)
        assertEquals(0, count)
    }

    @Test
    fun `parse filters out tasks with blank descriptions after parsing`() {
        // Some inputs might have special characters that parse to nothing
        val input = "Buy milk, , Call dentist"
        val result = BrainDumpParser.parse(input)

        assertEquals(2, result.size)
        assertEquals("Buy milk", result[0].description)
        assertEquals("Call dentist", result[1].description)
    }

    @Test
    fun `parse complex mixed format with metadata`() {
        val input = """
            Buy groceries tomorrow #personal @shopping
            Call dentist p1, Schedule meeting next monday #work
            Review pull requests p2 @review, Fix bug #urgent
        """.trimIndent()
        val result = BrainDumpParser.parse(input)

        assertEquals(5, result.size)

        // Task 1: Buy groceries
        assertEquals("Buy groceries", result[0].description)
        assertEquals("personal", result[0].project)
        assertTrue(result[0].tags.contains("shopping"))
        assertTrue(result[0].dueDate != null)

        // Task 2: Call dentist
        assertEquals("Call dentist", result[1].description)
        assertEquals("HIGH", result[1].priority?.name)

        // Task 3: Schedule meeting
        assertEquals("Schedule meeting", result[2].description)
        assertEquals("work", result[2].project)
        assertTrue(result[2].dueDate != null)

        // Task 4: Review pull requests
        assertEquals("Review pull requests", result[3].description)
        assertEquals("MEDIUM", result[3].priority?.name)
        assertTrue(result[3].tags.contains("review"))

        // Task 5: Fix bug
        assertEquals("Fix bug", result[4].description)
        assertEquals("urgent", result[4].project)
    }
}
