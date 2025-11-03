package com.taskhero.domain.task.usecase

import com.taskhero.core.parser.RecurrenceParser
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DateTimeUnit
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to generate recurring task instances from a template task.
 *
 * This use case:
 * - Takes a template task with a recurrence pattern (recur field)
 * - Parses the recurrence string to determine the interval
 * - Calculates the next N due dates based on the pattern
 * - Generates new task instances with proper parent UUID and imask
 * - Respects the "until" field to limit generation
 * - Returns a list of generated task instances
 */
class GenerateRecurringTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Generate recurring task instances from a template.
     *
     * @param template The template task with recur field set
     * @param count Number of instances to generate (default: 1)
     * @return Result containing the list of generated tasks
     */
    suspend operator fun invoke(template: Task, count: Int = 1): Result<List<Task>> {
        return try {
            // Validate that this is a recurring template
            if (template.recur == null) {
                return Result.failure(IllegalArgumentException("Task does not have a recurrence pattern"))
            }

            // Parse the recurrence pattern
            val recurrenceInfo = RecurrenceParser.parseToDateTimeUnit(template.recur)
                ?: return Result.failure(IllegalArgumentException("Invalid recurrence pattern: ${template.recur}"))

            val (amount, unit) = recurrenceInfo

            // Determine the base date to start from
            // Priority: due date > scheduled date > current time
            val baseDateMillis = template.due ?: template.scheduled ?: System.currentTimeMillis()

            // Get the next imask value (find highest existing imask for this template)
            val existingInstances = getExistingInstances(template.uuid)
            val nextImask = (existingInstances.maxOfOrNull { it.imask ?: 0 } ?: 0) + 1

            // Generate instances
            val generatedTasks = mutableListOf<Task>()

            for (i in 0 until count) {
                val instanceNumber = nextImask + i
                val newDueDate = calculateNextDueDate(baseDateMillis, amount, unit, instanceNumber)

                // Check if we've exceeded the "until" limit
                if (template.until != null && newDueDate > template.until) {
                    break
                }

                // Create new task instance
                val newTask = createTaskInstance(
                    template = template,
                    dueDate = newDueDate,
                    imask = instanceNumber
                )

                // Insert the task
                val insertResult = repository.insertTask(newTask)
                if (insertResult.isFailure) {
                    return Result.failure(
                        insertResult.exceptionOrNull()
                            ?: Exception("Failed to insert recurring task instance")
                    )
                }

                generatedTasks.add(newTask)
            }

            Result.success(generatedTasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get existing instances for a template task.
     */
    private suspend fun getExistingInstances(templateUuid: String): List<Task> {
        return try {
            var tasks = emptyList<Task>()
            repository.getTasksByParent(templateUuid).collect { taskList ->
                tasks = taskList
            }
            tasks
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Calculate the next due date based on recurrence pattern.
     */
    private fun calculateNextDueDate(
        baseDateMillis: Long,
        amount: Int,
        unit: RecurrenceParser.RecurrenceUnit,
        instanceNumber: Int
    ): Long {
        val baseInstant = Instant.fromEpochMilliseconds(baseDateMillis)
        val timeZone = TimeZone.currentSystemDefault()
        val baseDateTime = baseInstant.toLocalDateTime(timeZone)

        val newDateTime = when (unit) {
            RecurrenceParser.RecurrenceUnit.DAY -> {
                baseDateTime.date.plus(amount * instanceNumber, DateTimeUnit.DAY)
                    .atTime(baseDateTime.time)
            }
            RecurrenceParser.RecurrenceUnit.WEEK -> {
                baseDateTime.date.plus(amount * instanceNumber * 7, DateTimeUnit.DAY)
                    .atTime(baseDateTime.time)
            }
            RecurrenceParser.RecurrenceUnit.MONTH -> {
                baseDateTime.date.plus(amount * instanceNumber, DateTimeUnit.MONTH)
                    .atTime(baseDateTime.time)
            }
            RecurrenceParser.RecurrenceUnit.YEAR -> {
                baseDateTime.date.plus(amount * instanceNumber, DateTimeUnit.YEAR)
                    .atTime(baseDateTime.time)
            }
        }

        return newDateTime.toInstant(timeZone).toEpochMilliseconds()
    }

    /**
     * Create a new task instance from a template.
     */
    private fun createTaskInstance(
        template: Task,
        dueDate: Long,
        imask: Int
    ): Task {
        val currentTime = System.currentTimeMillis()

        return template.copy(
            uuid = UUID.randomUUID().toString(),
            status = TaskStatus.PENDING,
            entry = currentTime,
            modified = currentTime,
            start = null,
            end = null,
            due = dueDate,
            scheduled = dueDate, // Also update scheduled to match due
            parent = template.uuid,
            imask = imask,
            // Clear the mask field for instances
            mask = null
        )
    }
}
