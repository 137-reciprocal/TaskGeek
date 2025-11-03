package com.taskhero.feature.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taskhero.domain.report.model.TaskStatistics

/**
 * Composable that displays task statistics as a grid of cards.
 *
 * @param statistics The task statistics to display
 * @param modifier Modifier for the container
 */
@Composable
fun StatisticsCards(
    statistics: TaskStatistics,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(16.dp)
    ) {
        item {
            StatCard(
                title = "Total Tasks",
                value = statistics.totalTasks.toString(),
                subtitle = "All time"
            )
        }

        item {
            StatCard(
                title = "Completed",
                value = statistics.completedTasks.toString(),
                subtitle = "${(statistics.completionRate * 100).toInt()}% completion"
            )
        }

        item {
            StatCard(
                title = "Pending",
                value = statistics.pendingTasks.toString(),
                subtitle = "Active tasks"
            )
        }

        item {
            StatCard(
                title = "Overdue",
                value = statistics.overdueCount.toString(),
                subtitle = "Needs attention"
            )
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            CompletionRateCard(
                completionRate = statistics.completionRate,
                completedTasks = statistics.completedTasks,
                totalTasks = statistics.totalTasks
            )
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            StatCard(
                title = "Average Urgency",
                value = String.format("%.2f", statistics.averageUrgency),
                subtitle = "Across all tasks"
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompletionRateCard(
    completionRate: Float,
    completedTasks: Int,
    totalTasks: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completion Rate",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$completedTasks of $totalTasks tasks completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
