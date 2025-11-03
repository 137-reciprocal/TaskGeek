package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import javax.inject.Inject
import kotlin.math.max

/**
 * Use case to calculate task urgency using the 14-coefficient algorithm.
 *
 * Urgency factors:
 * - Due date proximity (variable weight based on how close)
 * - Priority: H=6.0, M=3.9, L=1.8
 * - Tags: "next" tag = +15.0
 * - Active status = +4.0
 * - Scheduled status = +5.0
 * - Blocking dependencies (tasks that depend on this one)
 * - Blocked by dependencies (tasks this one depends on)
 * - Age factor (older tasks get slight urgency boost)
 */
class CalculateUrgencyUseCase @Inject constructor() {

    companion object {
        // Priority coefficients
        private const val PRIORITY_HIGH = 6.0
        private const val PRIORITY_MEDIUM = 3.9
        private const val PRIORITY_LOW = 1.8

        // Tag coefficients
        private const val TAG_NEXT = 15.0

        // Status coefficients
        private const val ACTIVE_COEFFICIENT = 4.0
        private const val SCHEDULED_COEFFICIENT = 5.0

        // Due date coefficients (based on proximity)
        private const val DUE_IMMINENT = 12.0  // < 24 hours
        private const val DUE_VERY_SOON = 9.0  // < 3 days
        private const val DUE_SOON = 6.0        // < 7 days
        private const val DUE_NEAR = 3.0        // < 14 days
        private const val DUE_FAR = 1.5         // < 30 days
        private const val DUE_DISTANT = 0.2     // > 30 days

        // Dependency coefficients
        private const val BLOCKING_COEFFICIENT = 8.0
        private const val BLOCKED_COEFFICIENT = -5.0

        // Age coefficient (per day)
        private const val AGE_COEFFICIENT = 0.1
        private const val MAX_AGE_BONUS = 2.0

        // Time constants (milliseconds)
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }

    operator fun invoke(task: Task, blockingCount: Int = 0, blockedCount: Int = 0): Double {
        var urgency = 0.0

        // Priority coefficient
        urgency += when (task.priority) {
            TaskPriority.HIGH -> PRIORITY_HIGH
            TaskPriority.MEDIUM -> PRIORITY_MEDIUM
            TaskPriority.LOW -> PRIORITY_LOW
            else -> 0.0
        }

        // Due date coefficient (variable by proximity)
        task.due?.let { dueTime ->
            val currentTime = System.currentTimeMillis()
            val daysUntilDue = (dueTime - currentTime).toDouble() / DAY_MS

            urgency += when {
                daysUntilDue < 0 -> 15.0  // Overdue!
                daysUntilDue < 1 -> DUE_IMMINENT
                daysUntilDue < 3 -> DUE_VERY_SOON
                daysUntilDue < 7 -> DUE_SOON
                daysUntilDue < 14 -> DUE_NEAR
                daysUntilDue < 30 -> DUE_FAR
                else -> DUE_DISTANT
            }
        }

        // Tags coefficient
        if (task.tags.any { it.equals("next", ignoreCase = true) }) {
            urgency += TAG_NEXT
        }

        // Active status coefficient (has start time)
        if (task.start != null) {
            urgency += ACTIVE_COEFFICIENT
        }

        // Scheduled coefficient
        if (task.scheduled != null) {
            urgency += SCHEDULED_COEFFICIENT
        }

        // Blocking dependencies (tasks that depend on this one)
        urgency += blockingCount * BLOCKING_COEFFICIENT

        // Blocked by dependencies (tasks this one depends on)
        urgency += blockedCount * BLOCKED_COEFFICIENT

        // Age factor (older tasks get slight boost)
        val currentTime = System.currentTimeMillis()
        val ageDays = (currentTime - task.entry).toDouble() / DAY_MS
        val ageBonus = kotlin.math.min(ageDays * AGE_COEFFICIENT, MAX_AGE_BONUS)
        urgency += ageBonus

        // Ensure urgency is non-negative
        return max(0.0, urgency)
    }
}
