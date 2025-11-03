package com.taskhero.feature.taskdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taskhero.domain.task.model.TaskPriority

/**
 * Priority selector component showing chips for High, Medium, Low, and None.
 *
 * @param selectedPriority The currently selected priority
 * @param onPrioritySelected Callback when a priority is selected
 * @param modifier Modifier for the row
 */
@Composable
fun PrioritySelector(
    selectedPriority: TaskPriority?,
    onPrioritySelected: (TaskPriority?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // High priority
        FilterChip(
            selected = selectedPriority == TaskPriority.HIGH,
            onClick = {
                onPrioritySelected(
                    if (selectedPriority == TaskPriority.HIGH) null else TaskPriority.HIGH
                )
            },
            label = { Text("H") }
        )

        // Medium priority
        FilterChip(
            selected = selectedPriority == TaskPriority.MEDIUM,
            onClick = {
                onPrioritySelected(
                    if (selectedPriority == TaskPriority.MEDIUM) null else TaskPriority.MEDIUM
                )
            },
            label = { Text("M") }
        )

        // Low priority
        FilterChip(
            selected = selectedPriority == TaskPriority.LOW,
            onClick = {
                onPrioritySelected(
                    if (selectedPriority == TaskPriority.LOW) null else TaskPriority.LOW
                )
            },
            label = { Text("L") }
        )

        // None (explicitly setting to NONE or null)
        FilterChip(
            selected = selectedPriority == null || selectedPriority == TaskPriority.NONE,
            onClick = { onPrioritySelected(null) },
            label = { Text("None") }
        )
    }
}
