package com.taskhero.feature.tasklist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.taskhero.core.parser.ParsedTaskData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Preview list showing parsed tasks before they are added.
 *
 * Features:
 * - Numbered list (1, 2, 3...)
 * - Shows parsed metadata (date, priority, project, tags)
 * - Edit button per task
 * - Delete button per task
 *
 * @param parsedTasks List of parsed tasks to preview
 * @param onEditTask Callback when a task should be edited
 * @param onDeleteTask Callback when a task should be deleted
 * @param modifier Modifier for the component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrainDumpPreview(
    parsedTasks: List<ParsedTaskData>,
    onEditTask: (index: Int, newDescription: String) -> Unit,
    onDeleteTask: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = parsedTasks,
            key = { index, _ -> index }
        ) { index, task ->
            TaskPreviewCard(
                index = index,
                task = task,
                onEdit = { onEditTask(index, task.description) },
                onDelete = { onDeleteTask(index) }
            )
        }
    }
}

/**
 * Card displaying a single task preview.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskPreviewCard(
    index: Int,
    task: ParsedTaskData,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Task number
            Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Description
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Metadata chips
                if (hasMetadata(task)) {
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Due date chip
                        task.dueDate?.let { dueDate ->
                            MetadataChip(
                                label = formatDate(dueDate),
                                color = Color(0xFFEF5350), // Red
                                icon = "📅"
                            )
                        }

                        // Priority chip
                        task.priority?.let { priority ->
                            MetadataChip(
                                label = priority.name,
                                color = Color(0xFFFF9800), // Orange
                                icon = when (priority.name) {
                                    "HIGH" -> "🔥"
                                    "MEDIUM" -> "⚡"
                                    "LOW" -> "💤"
                                    else -> "⭐"
                                }
                            )
                        }

                        // Project chip
                        task.project?.let { project ->
                            MetadataChip(
                                label = project,
                                color = Color(0xFF2196F3), // Blue
                                icon = "#"
                            )
                        }

                        // Tag chips
                        task.tags.forEach { tag ->
                            MetadataChip(
                                label = tag,
                                color = Color(0xFF9C27B0), // Purple
                                icon = "@"
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit task ${index + 1}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task ${index + 1}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Metadata chip component.
 */
@Composable
private fun MetadataChip(
    label: String,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { },
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = color.copy(alpha = 0.3f)
        ),
        modifier = modifier
    )
}

/**
 * Check if task has any metadata.
 */
private fun hasMetadata(task: ParsedTaskData): Boolean {
    return task.dueDate != null ||
            task.priority != null ||
            task.project != null ||
            task.tags.isNotEmpty()
}

/**
 * Format timestamp to readable date string.
 */
private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
