package com.taskhero.feature.tasklist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskhero.core.ui.accessibility.AccessibilityConstants
import com.taskhero.core.ui.accessibility.semanticDescription
import com.taskhero.core.ui.accessibility.semanticRole
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Card component to display a single task.
 * Shows task description, due date, priority, and urgency.
 *
 * @param task The task to display
 * @param onComplete Callback when task is marked complete
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card
 */
@Composable
fun TaskCard(
    task: Task,
    onComplete: (String) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(task.uuid) }
            .semanticDescription(
                AccessibilityConstants.TaskList.taskCard(
                    description = task.description,
                    status = task.status.name,
                    priority = task.priority?.name
                )
            )
            .semanticRole(Role.Button),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion checkbox
            IconButton(
                onClick = { onComplete(task.uuid) },
                enabled = task.status != TaskStatus.COMPLETED,
                modifier = Modifier.semanticDescription(
                    if (task.status == TaskStatus.COMPLETED) {
                        AccessibilityConstants.TaskList.completedTask(task.description)
                    } else {
                        AccessibilityConstants.TaskList.COMPLETE_TASK_BUTTON
                    }
                )
            ) {
                Icon(
                    imageVector = if (task.status == TaskStatus.COMPLETED) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Outlined.Circle
                    },
                    contentDescription = null, // Handled by IconButton semantics
                    tint = if (task.status == TaskStatus.COMPLETED) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Task description
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Task metadata row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority
                    task.priority?.let { priority ->
                        PriorityChip(priority = priority)
                    }

                    // Urgency
                    if (task.urgency > 0) {
                        Text(
                            text = "Urgency: ${"%.1f".format(task.urgency)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                task.urgency >= 10.0 -> MaterialTheme.colorScheme.error
                                task.urgency >= 5.0 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    // Due date
                    task.due?.let { dueTimestamp ->
                        Text(
                            text = "Due: ${formatDate(dueTimestamp)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isOverdue(dueTimestamp)) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                // Project
                task.project?.let { project ->
                    Text(
                        text = "Project: $project",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Tags
                if (task.tags.isNotEmpty()) {
                    Text(
                        text = task.tags.joinToString(", ") { "+$it" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Chip to display task priority.
 */
@Composable
private fun PriorityChip(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (priority) {
        TaskPriority.HIGH -> "H" to MaterialTheme.colorScheme.error
        TaskPriority.MEDIUM -> "M" to MaterialTheme.colorScheme.tertiary
        TaskPriority.LOW -> "L" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
    )
}

/**
 * Format timestamp to readable date string.
 */
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

/**
 * Check if a due date is overdue.
 */
private fun isOverdue(dueTimestamp: Long): Boolean {
    return dueTimestamp < System.currentTimeMillis()
}
