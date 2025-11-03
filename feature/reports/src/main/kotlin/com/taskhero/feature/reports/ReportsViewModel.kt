package com.taskhero.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskhero.domain.report.model.DateRange
import com.taskhero.domain.report.usecase.GetBurndownDataUseCase
import com.taskhero.domain.report.usecase.GetTaskStatisticsUseCase
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.usecase.GetTasksUseCase
import com.taskhero.domain.timetracking.repository.TimeTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * ViewModel for Reports screen following MVI pattern.
 * Manages state, handles user intents, and emits side effects.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getBurndownDataUseCase: GetBurndownDataUseCase,
    private val getTaskStatisticsUseCase: GetTaskStatisticsUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val timeTrackingRepository: TimeTrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ReportsEffect>()
    val effect: SharedFlow<ReportsEffect> = _effect.asSharedFlow()

    private var currentDateRange: DateRange = DateRange.lastNDays(30)

    init {
        loadReports()
    }

    /**
     * Handle user intents.
     */
    fun onIntent(intent: ReportsIntent) {
        when (intent) {
            is ReportsIntent.LoadReports -> loadReports()
            is ReportsIntent.ChangeDateRange -> changeDateRange(intent.dateRange)
        }
    }

    /**
     * Load reports data from use cases.
     */
    private fun loadReports() {
        viewModelScope.launch {
            _uiState.value = ReportsUiState.Loading

            // Calculate week and month date ranges
            val now = LocalDate.now()
            val startOfWeek = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            val endOfWeek = now.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
            val startOfMonth = now.withDayOfMonth(1)
            val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())

            val startOfWeekMillis = startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfWeekMillis = endOfWeek.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val startOfMonthMillis = startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonthMillis = endOfMonth.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            combine(
                getBurndownDataUseCase(currentDateRange),
                getTaskStatisticsUseCase(),
                getTasksUseCase(),
                timeTrackingRepository.getTimeByProject(),
                timeTrackingRepository.getTimeByDay(
                    currentDateRange.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    currentDateRange.endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                ),
                timeTrackingRepository.getTotalTimeInRange(startOfWeekMillis, endOfWeekMillis),
                timeTrackingRepository.getTotalTimeInRange(startOfMonthMillis, endOfMonthMillis)
            ) { burndownData, statistics, tasks, timeByProject, timeByDay, weekTime, monthTime ->
                ReportsData(burndownData, statistics, tasks, timeByProject, timeByDay, weekTime, monthTime)
            }
                .catch { exception ->
                    _uiState.value = ReportsUiState.Error(
                        message = exception.message ?: "Failed to load reports"
                    )
                    _effect.emit(ReportsEffect.ShowSnackbar("Failed to load reports"))
                }
                .collect { data ->
                    val calendarTasks = buildCalendarTasksMap(data.tasks, currentDateRange)

                    _uiState.value = ReportsUiState.Success(
                        burndownData = data.burndownData.toImmutableList(),
                        calendarTasks = calendarTasks,
                        statistics = data.statistics,
                        dateRange = currentDateRange,
                        timeByProject = data.timeByProject.toImmutableMap(),
                        timeByDay = data.timeByDay.toImmutableMap(),
                        totalTimeThisWeek = data.totalTimeThisWeek,
                        totalTimeThisMonth = data.totalTimeThisMonth
                    )
                }
        }
    }

    /**
     * Data class to hold all reports data.
     */
    private data class ReportsData(
        val burndownData: List<com.taskhero.domain.report.model.BurndownPoint>,
        val statistics: com.taskhero.domain.report.model.TaskStatistics,
        val tasks: List<Task>,
        val timeByProject: Map<String, Long>,
        val timeByDay: Map<Long, Long>,
        val totalTimeThisWeek: Long,
        val totalTimeThisMonth: Long
    )

    /**
     * Change the date range for reports.
     */
    private fun changeDateRange(dateRange: DateRange) {
        currentDateRange = dateRange
        loadReports()
    }

    /**
     * Build a map of dates to tasks for the calendar view.
     */
    private fun buildCalendarTasksMap(
        tasks: List<Task>,
        dateRange: DateRange
    ): kotlinx.collections.immutable.ImmutableMap<LocalDate, kotlinx.collections.immutable.ImmutableList<Task>> {
        val tasksMap = mutableMapOf<LocalDate, MutableList<Task>>()

        tasks.forEach { task ->
            task.due?.let { dueMillis ->
                val dueDate = Instant.ofEpochMilli(dueMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                if (!dueDate.isBefore(dateRange.startDate) && !dueDate.isAfter(dateRange.endDate)) {
                    tasksMap.getOrPut(dueDate) { mutableListOf() }.add(task)
                }
            }
        }

        return tasksMap.mapValues { it.value.toImmutableList() }.toImmutableMap()
    }
}
