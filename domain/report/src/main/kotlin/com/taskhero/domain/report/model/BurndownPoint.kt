package com.taskhero.domain.report.model

import java.time.LocalDate

/**
 * Represents a single data point in a burndown chart.
 *
 * @property date The date of this data point
 * @property pendingCount Number of pending tasks on this date
 * @property completedCount Number of completed tasks on this date
 */
data class BurndownPoint(
    val date: LocalDate,
    val pendingCount: Int,
    val completedCount: Int
)
