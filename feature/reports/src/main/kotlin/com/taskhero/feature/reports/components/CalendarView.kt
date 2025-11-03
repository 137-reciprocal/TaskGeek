package com.taskhero.feature.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taskhero.domain.task.model.Task
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Composable that displays a calendar grid with task indicators.
 * Shows which days have tasks due.
 *
 * @param calendarTasks Map of dates to tasks due on those dates
 * @param currentMonth The month to display
 * @param onDateClick Callback when a date is clicked
 * @param modifier Modifier for the calendar container
 */
@Composable
fun CalendarView(
    calendarTasks: ImmutableMap<LocalDate, ImmutableList<Task>>,
    currentMonth: YearMonth = YearMonth.now(),
    onDateClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val daysInMonth = remember(currentMonth) {
        val firstDay = currentMonth.atDay(1)
        val lastDay = currentMonth.atEndOfMonth()
        val startDayOfWeek = firstDay.dayOfWeek.value % 7 // 0 = Sunday
        val totalDays = lastDay.dayOfMonth

        buildList {
            // Add empty cells for days before the first day
            repeat(startDayOfWeek) {
                add(null)
            }
            // Add all days of the month
            for (day in 1..totalDays) {
                add(currentMonth.atDay(day))
            }
        }
    }

    Column(modifier = modifier) {
        // Month header
        Text(
            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                    " ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center
        )

        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(daysInMonth) { date ->
                CalendarDay(
                    date = date,
                    taskCount = date?.let { calendarTasks[it]?.size ?: 0 } ?: 0,
                    onClick = { date?.let(onDateClick) }
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    taskCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .then(
                if (date != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
            .background(
                color = when {
                    date == null -> MaterialTheme.colorScheme.surface
                    taskCount > 0 -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = MaterialTheme.shapes.small
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (taskCount > 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (taskCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = taskCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
