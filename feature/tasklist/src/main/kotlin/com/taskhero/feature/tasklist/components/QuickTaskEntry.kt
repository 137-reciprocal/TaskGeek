package com.taskhero.feature.tasklist.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.taskhero.core.parser.NaturalLanguageParser
import com.taskhero.core.parser.ParsedTaskData
import com.taskhero.core.ui.accessibility.AccessibilityConstants
import com.taskhero.core.ui.accessibility.semanticDescription
import com.taskhero.core.ui.accessibility.semanticRole
import com.taskhero.domain.task.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Quick task entry bar with natural language parsing.
 * Allows users to quickly create tasks using syntax like:
 * "task description tomorrow #project @tag p1"
 *
 * @param onCreateTask Callback when task should be created with parsed data
 * @param onExpandToFull Callback to open full task detail editor
 * @param isMinimized Whether the quick entry is minimized
 * @param onToggleMinimized Callback to toggle minimized state
 * @param modifier Modifier for the component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickTaskEntry(
    onCreateTask: (ParsedTaskData) -> Unit,
    onExpandToFull: () -> Unit,
    isMinimized: Boolean = false,
    onToggleMinimized: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    var parsedData by remember { mutableStateOf(ParsedTaskData(description = "")) }

    // Parse input in real-time
    LaunchedEffect(input) {
        parsedData = NaturalLanguageParser.parse(input)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        AnimatedVisibility(
            visible = !isMinimized,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header row with minimize button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Add Task",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = onToggleMinimized,
                        modifier = Modifier.semanticDescription("Minimize quick entry")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = null
                        )
                    }
                }

                // Input field with annotated text
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semanticDescription(AccessibilityConstants.TaskList.QUICK_ENTRY_FIELD),
                    placeholder = {
                        Text("What do you want to accomplish?")
                    },
                    supportingText = {
                        Text(
                            text = "Try: tomorrow #project @tag p1",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = false,
                    maxLines = 3
                )

                // Parsed elements as colored chips
                AnimatedVisibility(
                    visible = hasAnyParsedElements(parsedData),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Due date chip
                        parsedData.dueDate?.let { dueDate ->
                            ParsedElementChip(
                                label = "Due: ${formatDate(dueDate)}",
                                color = Color(0xFFEF5350) // Red
                            )
                        }

                        // Priority chip
                        parsedData.priority?.let { priority ->
                            ParsedElementChip(
                                label = "Priority: ${priority.name}",
                                color = Color(0xFFFF9800) // Orange
                            )
                        }

                        // Project chip
                        parsedData.project?.let { project ->
                            ParsedElementChip(
                                label = "Project: $project",
                                color = Color(0xFF2196F3) // Blue
                            )
                        }

                        // Tag chips
                        parsedData.tags.forEach { tag ->
                            ParsedElementChip(
                                label = "Tag: $tag",
                                color = Color(0xFF9C27B0) // Purple
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    // Expand to full button
                    TextButton(
                        onClick = onExpandToFull,
                        modifier = Modifier.semanticDescription("Open full task editor")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Full Editor")
                    }

                    // Create task button
                    Button(
                        onClick = {
                            if (parsedData.description.isNotBlank()) {
                                onCreateTask(parsedData)
                                input = "" // Clear input after creation
                            }
                        },
                        enabled = parsedData.description.isNotBlank(),
                        modifier = Modifier.semanticDescription(AccessibilityConstants.TaskList.CREATE_QUICK_TASK_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Add Task")
                    }
                }
            }
        }

        // Minimized view
        AnimatedVisibility(
            visible = isMinimized,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Add Task",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onToggleMinimized,
                    modifier = Modifier.semanticDescription("Expand quick entry")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

/**
 * Chip to display a parsed element with colored background.
 */
@Composable
private fun ParsedElementChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color.copy(alpha = 0.9f)
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = color.copy(alpha = 0.5f)
        ),
        modifier = modifier.semanticRole(Role.Button)
    )
}

/**
 * Check if parsed data has any elements besides the description.
 */
private fun hasAnyParsedElements(parsedData: ParsedTaskData): Boolean {
    return parsedData.dueDate != null ||
            parsedData.priority != null ||
            parsedData.project != null ||
            parsedData.tags.isNotEmpty()
}

/**
 * Format timestamp to readable date string.
 */
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
