package com.taskhero.feature.reports

import com.taskhero.domain.report.model.BurndownPoint
import com.taskhero.domain.report.model.DateRange
import com.taskhero.domain.report.model.TaskStatistics
import com.taskhero.domain.task.model.Task
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import java.time.LocalDate

/**
 * UI State for Reports screen following MVI pattern.
 * Represents all possible states of the reports UI.
 */
sealed interface ReportsUiState {
    /**
     * Loading state while fetching report data.
     */
    data object Loading : ReportsUiState

    /**
     * Success state with loaded report data.
     *
     * @property burndownData List of burndown chart data points
     * @property calendarTasks Map of dates to tasks due on those dates
     * @property statistics Aggregated task statistics
     * @property dateRange Current date range for reports
     * @property timeByProject Map of project names to total time in milliseconds
     * @property timeByDay Map of dates to total time in milliseconds
     * @property totalTimeThisWeek Total time tracked this week in milliseconds
     * @property totalTimeThisMonth Total time tracked this month in milliseconds
     */
    data class Success(
        val burndownData: ImmutableList<BurndownPoint> = persistentListOf(),
        val calendarTasks: ImmutableMap<LocalDate, ImmutableList<Task>> = persistentMapOf(),
        val statistics: TaskStatistics,
        val dateRange: DateRange,
        val timeByProject: ImmutableMap<String, Long> = persistentMapOf(),
        val timeByDay: ImmutableMap<Long, Long> = persistentMapOf(),
        val totalTimeThisWeek: Long = 0L,
        val totalTimeThisMonth: Long = 0L
    ) : ReportsUiState

    /**
     * Error state when report loading fails.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : ReportsUiState
}
