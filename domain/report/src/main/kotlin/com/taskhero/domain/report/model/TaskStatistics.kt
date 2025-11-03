package com.taskhero.domain.report.model

/**
 * Represents aggregated statistics about tasks.
 *
 * @property totalTasks Total number of tasks
 * @property completedTasks Number of completed tasks
 * @property pendingTasks Number of pending tasks
 * @property overdueCount Number of overdue tasks
 * @property completionRate Percentage of completed tasks (0.0 to 1.0)
 * @property averageUrgency Average urgency score across all tasks
 */
data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val overdueCount: Int,
    val completionRate: Float,
    val averageUrgency: Double
)
