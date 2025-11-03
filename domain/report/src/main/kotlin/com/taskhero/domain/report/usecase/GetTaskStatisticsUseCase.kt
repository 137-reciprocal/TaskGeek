package com.taskhero.domain.report.usecase

import com.taskhero.domain.report.model.TaskStatistics
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case to calculate task statistics.
 * Aggregates various metrics about tasks such as completion rate, overdue count, etc.
 */
class GetTaskStatisticsUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<TaskStatistics> {
        return taskRepository.getAllTasks().map { tasks ->
            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
            val pendingTasks = tasks.count { it.status == TaskStatus.PENDING }

            val now = System.currentTimeMillis()
            val overdueCount = tasks.count { task ->
                task.status == TaskStatus.PENDING &&
                task.due != null &&
                task.due!! < now
            }

            val completionRate = if (totalTasks > 0) {
                completedTasks.toFloat() / totalTasks.toFloat()
            } else {
                0f
            }

            val averageUrgency = if (tasks.isNotEmpty()) {
                tasks.map { it.urgency }.average()
            } else {
                0.0
            }

            TaskStatistics(
                totalTasks = totalTasks,
                completedTasks = completedTasks,
                pendingTasks = pendingTasks,
                overdueCount = overdueCount,
                completionRate = completionRate,
                averageUrgency = averageUrgency
            )
        }
    }
}
