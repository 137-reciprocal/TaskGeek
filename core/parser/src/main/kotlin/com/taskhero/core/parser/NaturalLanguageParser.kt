package com.taskhero.core.parser

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import com.taskhero.domain.task.model.TaskPriority
import kotlinx.datetime.*

/**
 * Parsed task data from natural language input.
 *
 * @property description The task description (remaining text after parsing)
 * @property dueDate Parsed due date timestamp in milliseconds, null if not specified
 * @property priority Parsed priority, null if not specified
 * @property project Parsed project name, null if not specified
 * @property tags List of parsed tags
 * @property annotatedString Formatted string with colored spans for UI display
 */
data class ParsedTaskData(
    val description: String,
    val dueDate: Long? = null,
    val priority: TaskPriority? = null,
    val project: String? = null,
    val tags: List<String> = emptyList(),
    val annotatedString: AnnotatedString = AnnotatedString(description)
)

/**
 * Parses natural language task input into structured task data.
 *
 * Supported syntax:
 * - Due dates: "tomorrow", "today", "next monday", "+3d", "2025-12-31"
 * - Priority: "p1" or "!1" = HIGH, "p2" or "!2" = MEDIUM, "p3" or "!3" = LOW
 * - Project: "#projectname"
 * - Tags: "@tagname"
 *
 * Example: "Buy groceries tomorrow #personal @shopping p1"
 */
object NaturalLanguageParser {

    // Color schemes for different element types
    private val DUE_DATE_COLOR = Color(0xFFEF5350) // Red
    private val PRIORITY_COLOR = Color(0xFFFF9800) // Orange
    private val PROJECT_COLOR = Color(0xFF2196F3) // Blue
    private val TAG_COLOR = Color(0xFF9C27B0) // Purple

    /**
     * Parse natural language input into structured task data.
     *
     * @param input The natural language task description
     * @return ParsedTaskData with extracted elements and highlighted text
     */
    fun parse(input: String): ParsedTaskData {
        if (input.isBlank()) {
            return ParsedTaskData(description = "", annotatedString = AnnotatedString(""))
        }

        val tokens = tokenize(input)
        val elements = mutableListOf<ParsedElement>()
        val descriptionTokens = mutableListOf<String>()

        for (token in tokens) {
            val element = parseToken(token)
            if (element != null) {
                elements.add(element)
            } else {
                descriptionTokens.add(token)
            }
        }

        // Extract parsed values
        val dueDate = elements.filterIsInstance<ParsedElement.DueDate>().firstOrNull()?.timestamp
        val priority = elements.filterIsInstance<ParsedElement.Priority>().firstOrNull()?.priority
        val project = elements.filterIsInstance<ParsedElement.Project>().firstOrNull()?.name
        val tags = elements.filterIsInstance<ParsedElement.Tag>().map { it.name }

        // Build description from remaining tokens
        val description = descriptionTokens.joinToString(" ").trim()

        // Build annotated string with colored elements
        val annotatedString = buildAnnotatedString(input, elements)

        return ParsedTaskData(
            description = description,
            dueDate = dueDate,
            priority = priority,
            project = project,
            tags = tags,
            annotatedString = annotatedString
        )
    }

    /**
     * Tokenize input by splitting on whitespace while preserving the tokens.
     */
    private fun tokenize(input: String): List<String> {
        return input.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    /**
     * Parse a single token into a structured element.
     */
    private fun parseToken(token: String): ParsedElement? {
        // Try priority patterns first (p1, p2, p3, !1, !2, !3)
        parsePriority(token)?.let { return it }

        // Try project pattern (#projectname)
        parseProject(token)?.let { return it }

        // Try tag pattern (@tagname)
        parseTag(token)?.let { return it }

        // Try date expressions
        parseDate(token)?.let { return it }

        return null
    }

    /**
     * Parse priority from token (p1/!1 = HIGH, p2/!2 = MEDIUM, p3/!3 = LOW).
     */
    private fun parsePriority(token: String): ParsedElement.Priority? {
        val normalized = token.lowercase()
        val priority = when {
            normalized == "p1" || normalized == "!1" -> TaskPriority.HIGH
            normalized == "p2" || normalized == "!2" -> TaskPriority.MEDIUM
            normalized == "p3" || normalized == "!3" -> TaskPriority.LOW
            else -> null
        }
        return priority?.let { ParsedElement.Priority(token, it) }
    }

    /**
     * Parse project from token (#projectname).
     */
    private fun parseProject(token: String): ParsedElement.Project? {
        return if (token.startsWith("#") && token.length > 1) {
            ParsedElement.Project(token, token.substring(1))
        } else {
            null
        }
    }

    /**
     * Parse tag from token (@tagname).
     */
    private fun parseTag(token: String): ParsedElement.Tag? {
        return if (token.startsWith("@") && token.length > 1) {
            ParsedElement.Tag(token, token.substring(1))
        } else {
            null
        }
    }

    /**
     * Parse date from token using DateExpressionParser.
     */
    private fun parseDate(token: String): ParsedElement.DueDate? {
        val localDateTime = DateExpressionParser.parse(token) ?: return null

        // Convert LocalDateTime to epoch milliseconds
        val instant = localDateTime.toInstant(TimeZone.currentSystemDefault())
        val timestamp = instant.toEpochMilliseconds()

        return ParsedElement.DueDate(token, timestamp, localDateTime)
    }

    /**
     * Build an annotated string with colored spans for parsed elements.
     */
    private fun buildAnnotatedString(
        input: String,
        elements: List<ParsedElement>
    ): AnnotatedString {
        return buildAnnotatedString {
            var currentIndex = 0
            val inputLower = input.lowercase()

            // Find and highlight each element in order
            for (element in elements) {
                val tokenLower = element.originalToken.lowercase()
                val startIndex = inputLower.indexOf(tokenLower, currentIndex)

                if (startIndex != -1) {
                    // Add text before this element (unhighlighted)
                    if (startIndex > currentIndex) {
                        append(input.substring(currentIndex, startIndex))
                    }

                    // Add the element with appropriate color
                    val color = when (element) {
                        is ParsedElement.DueDate -> DUE_DATE_COLOR
                        is ParsedElement.Priority -> PRIORITY_COLOR
                        is ParsedElement.Project -> PROJECT_COLOR
                        is ParsedElement.Tag -> TAG_COLOR
                    }

                    withStyle(SpanStyle(color = color)) {
                        append(input.substring(startIndex, startIndex + element.originalToken.length))
                    }

                    currentIndex = startIndex + element.originalToken.length
                }
            }

            // Add remaining text
            if (currentIndex < input.length) {
                append(input.substring(currentIndex))
            }
        }
    }

    /**
     * Sealed class representing parsed elements from natural language input.
     */
    private sealed class ParsedElement(val originalToken: String) {
        data class DueDate(
            val token: String,
            val timestamp: Long,
            val localDateTime: LocalDateTime
        ) : ParsedElement(token)

        data class Priority(
            val token: String,
            val priority: TaskPriority
        ) : ParsedElement(token)

        data class Project(
            val token: String,
            val name: String
        ) : ParsedElement(token)

        data class Tag(
            val token: String,
            val name: String
        ) : ParsedElement(token)
    }
}
