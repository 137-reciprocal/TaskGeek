package com.taskhero.feature.tasklist.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
 * Card component to display a single task with premium UI/UX quality.
 * Inspired by Things 3 and Todoist design principles.
 *
 * Features:
 * - Priority color bar on the left edge
 * - Hover and pressed states with smooth animations
 * - Clean visual hierarchy
 * - Professional micro-interactions
 * - Accessible design with semantic structure
 *
 * @param task The task to display
 * @param onComplete Callback when task is marked complete
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: Task,
    onComplete: (String) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animation for scale when pressed
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 400f
        ),
        label = "cardScale"
    )

    // Animation for elevation
    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 1f
            isHovered -> 4f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 150),
        label = "cardElevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .semanticDescription(
                AccessibilityConstants.TaskList.taskCard(
                    description = task.description,
                    status = task.status.name,
                    priority = task.priority?.name
                )
            )
            .semanticRole(Role.Button),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    onClick = { onClick(task.uuid) }
                ),
            verticalAlignment = Alignment.Top
        ) {
            // Priority color bar (left edge)
            PriorityBar(priority = task.priority)

            // Completion checkbox
            CheckboxSection(
                task = task,
                onComplete = onComplete
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 14.dp, bottom = 14.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Task description (primary)
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Metadata row (secondary)
                MetadataRow(task = task)

                // Tags (if any)
                if (task.tags.isNotEmpty()) {
                    TagsSection(tags = task.tags)
                }
            }
        }
    }
}

/**
 * Priority color bar displayed on the left edge of the task card.
 */
@Composable
private fun PriorityBar(
    priority: TaskPriority?,
    modifier: Modifier = Modifier
) {
    val color = when (priority) {
        TaskPriority.HIGH -> Color(0xFFEF4444) // Red
        TaskPriority.MEDIUM -> Color(0xFFF97316) // Orange
        TaskPriority.LOW -> Color(0xFF22C55E) // Green
        null -> Color.Transparent
    }

    Box(
        modifier = modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(color)
    )
}

/**
 * Checkbox section for task completion with animation.
 */
@Composable
private fun CheckboxSection(
    task: Task,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkboxScale by animateFloatAsState(
        targetValue = if (task.status == TaskStatus.COMPLETED) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "checkboxScale"
    )

    IconButton(
        onClick = { onComplete(task.uuid) },
        enabled = task.status != TaskStatus.COMPLETED,
        modifier = modifier
            .padding(start = 8.dp, top = 8.dp)
            .size(40.dp)
            .semanticDescription(
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
            },
            modifier = Modifier
                .size(24.dp)
                .scale(checkboxScale)
        )
    }
}

/**
 * Metadata row displaying due date, project, and urgency.
 */
@Composable
private fun MetadataRow(
    task: Task,
    modifier: Modifier = Modifier
) {
    val hasMetadata = task.due != null || task.project != null || task.urgency > 0

    if (hasMetadata) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            // Due date
            task.due?.let { dueTimestamp ->
                val isTaskOverdue = isOverdue(dueTimestamp)
                MetadataItem(
                    text = formatDate(dueTimestamp),
                    color = if (isTaskOverdue) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (task.project != null || task.urgency > 0) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // Project
            task.project?.let { project ->
                MetadataItem(
                    text = project,
                    color = MaterialTheme.colorScheme.primary
                )

                if (task.urgency > 0) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // Urgency
            if (task.urgency > 0) {
                MetadataItem(
                    text = "U: ${"%.1f".format(task.urgency)}",
                    color = when {
                        task.urgency >= 10.0 -> MaterialTheme.colorScheme.error
                        task.urgency >= 5.0 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Individual metadata item with consistent styling.
 */
@Composable
private fun MetadataItem(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Tags section displaying task tags as chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        tags.forEach { tag ->
            TagChip(tag = tag)
        }
    }
}

/**
 * Individual tag chip with rounded shape.
 */
@Composable
private fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
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
