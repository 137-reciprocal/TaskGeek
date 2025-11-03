package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.XpReward
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import javax.inject.Inject
import kotlin.math.max

/**
 * Use case to calculate XP reward for completing a task.
 *
 * XP calculation factors:
 * - Base XP from task urgency
 * - Priority multiplier (High=2.0, Medium=1.5, Low=1.0)
 * - Due date bonus/penalty
 *   - Completed before due: bonus
 *   - Completed after due: penalty
 */
class CalculateXpRewardUseCase @Inject constructor() {

    companion object {
        // Base XP multiplier (urgency is converted to XP)
        private const val BASE_XP_MULTIPLIER = 10.0

        // Priority multipliers
        private const val PRIORITY_HIGH_MULTIPLIER = 2.0
        private const val PRIORITY_MEDIUM_MULTIPLIER = 1.5
        private const val PRIORITY_LOW_MULTIPLIER = 1.0

        // Due date bonus/penalty
        private const val DUE_DATE_EARLY_BONUS = 50L  // Completed before due date
        private const val DUE_DATE_LATE_PENALTY = -30L  // Completed after due date

        // Time constants
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }

    operator fun invoke(task: Task): XpReward {
        // Base XP from task urgency
        val baseXp = (task.urgency * BASE_XP_MULTIPLIER).toLong()

        // Priority multiplier
        val priorityMultiplier = when (task.priority) {
            TaskPriority.HIGH -> PRIORITY_HIGH_MULTIPLIER
            TaskPriority.MEDIUM -> PRIORITY_MEDIUM_MULTIPLIER
            TaskPriority.LOW -> PRIORITY_LOW_MULTIPLIER
            else -> 1.0
        }

        // Due date bonus/penalty
        val dueDateBonus = task.due?.let { dueTime ->
            val currentTime = System.currentTimeMillis()
            if (currentTime <= dueTime) {
                // Completed before or on due date - bonus
                DUE_DATE_EARLY_BONUS
            } else {
                // Completed after due date - penalty
                DUE_DATE_LATE_PENALTY
            }
        } ?: 0L

        // Calculate completion bonus (urgency multiplier is based on task characteristics)
        val urgencyMultiplier = when {
            task.urgency >= 15.0 -> 1.5  // Very urgent tasks
            task.urgency >= 10.0 -> 1.3  // Urgent tasks
            task.urgency >= 5.0 -> 1.1   // Moderate urgency
            else -> 1.0
        }

        return XpReward(
            baseXp = baseXp,
            urgencyMultiplier = urgencyMultiplier * priorityMultiplier,
            completionBonus = max(0L, dueDateBonus)  // Don't penalize XP, just no bonus
        )
    }
}
