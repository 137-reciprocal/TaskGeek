package com.taskhero.core.parser

/**
 * Parser for brain dump input that handles multiple tasks in various formats.
 *
 * Supported formats:
 * - Comma-separated: "task1, task2, task3"
 * - Newline-separated: Multi-line input with one task per line
 * - Mixed format: Combination of commas and newlines
 * - Natural language per task: Each task can contain natural language syntax
 *
 * Example inputs:
 * ```
 * Buy milk tomorrow, Call dentist p1, Review code #work
 * ```
 * ```
 * Buy milk tomorrow
 * Call dentist p1
 * Review code #work
 * ```
 */
object BrainDumpParser {

    /**
     * Separator detection threshold.
     * If input contains 2 or more commas, treat as comma-separated.
     */
    private const val COMMA_THRESHOLD = 2

    /**
     * Parse brain dump input into a list of parsed tasks.
     *
     * @param input Raw brain dump input text
     * @return List of ParsedTaskData, one for each detected task
     */
    fun parse(input: String): List<ParsedTaskData> {
        if (input.isBlank()) {
            return emptyList()
        }

        // Detect separator type
        val separatorMode = detectSeparatorMode(input)

        // Split input based on detected separator
        val rawTasks = when (separatorMode) {
            SeparatorMode.COMMA -> splitByComma(input)
            SeparatorMode.NEWLINE -> splitByNewline(input)
            SeparatorMode.MIXED -> splitByBoth(input)
        }

        // Parse each task using NaturalLanguageParser
        return rawTasks
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { taskText ->
                NaturalLanguageParser.parse(taskText)
            }
            .filter { it.description.isNotBlank() } // Filter out empty descriptions
    }

    /**
     * Detect which separator mode to use based on input characteristics.
     */
    private fun detectSeparatorMode(input: String): SeparatorMode {
        val commaCount = input.count { it == ',' }
        val newlineCount = input.count { it == '\n' }

        return when {
            // If 2+ commas and no newlines, use comma mode
            commaCount >= COMMA_THRESHOLD && newlineCount == 0 -> SeparatorMode.COMMA

            // If newlines but no/few commas, use newline mode
            newlineCount > 0 && commaCount < COMMA_THRESHOLD -> SeparatorMode.NEWLINE

            // If both commas and newlines, use mixed mode
            commaCount >= COMMA_THRESHOLD && newlineCount > 0 -> SeparatorMode.MIXED

            // Default to newline mode for single-line or simple input
            else -> SeparatorMode.NEWLINE
        }
    }

    /**
     * Split input by commas.
     */
    private fun splitByComma(input: String): List<String> {
        return input.split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * Split input by newlines.
     */
    private fun splitByNewline(input: String): List<String> {
        return input.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * Split input by both commas and newlines.
     * Splits by newlines first, then by commas within each line.
     */
    private fun splitByBoth(input: String): List<String> {
        return input.lines()
            .flatMap { line ->
                line.split(',')
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            }
    }

    /**
     * Count the number of tasks that will be parsed from the input.
     * Useful for showing real-time count to user.
     *
     * @param input Raw brain dump input text
     * @return Number of tasks that will be created
     */
    fun countTasks(input: String): Int {
        if (input.isBlank()) {
            return 0
        }

        val separatorMode = detectSeparatorMode(input)
        val rawTasks = when (separatorMode) {
            SeparatorMode.COMMA -> splitByComma(input)
            SeparatorMode.NEWLINE -> splitByNewline(input)
            SeparatorMode.MIXED -> splitByBoth(input)
        }

        return rawTasks
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .count()
    }

    /**
     * Separator mode enum.
     */
    private enum class SeparatorMode {
        COMMA,      // Comma-separated
        NEWLINE,    // Newline-separated
        MIXED       // Both commas and newlines
    }
}
