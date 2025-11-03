package com.taskhero.domain.report.usecase

import com.taskhero.domain.report.model.BurndownPoint
import com.taskhero.domain.report.model.DateRange
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Use case to get burndown chart data for a given date range.
 * Calculates the number of pending and completed tasks for each day.
 */
class GetBurndownDataUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(dateRange: DateRange): Flow<List<BurndownPoint>> {
        return taskRepository.getAllTasks().map { tasks ->
            val dates = generateDateSequence(dateRange)

            dates.map { date ->
                val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Count tasks that existed on this date
                val tasksOnDate = tasks.filter { task ->
                    task.entry < dayEnd
                }

                val pendingCount = tasksOnDate.count { task ->
                    task.status == TaskStatus.PENDING &&
                    (task.end == null || task.end!! >= dayStart)
                }

                val completedCount = tasksOnDate.count { task ->
                    task.status == TaskStatus.COMPLETED &&
                    task.end != null && task.end!! < dayEnd
                }

                BurndownPoint(
                    date = date,
                    pendingCount = pendingCount,
                    completedCount = completedCount
                )
            }
        }
    }

    private fun generateDateSequence(dateRange: DateRange): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var currentDate = dateRange.startDate

        while (!currentDate.isAfter(dateRange.endDate)) {
            dates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        return dates
    }
}
