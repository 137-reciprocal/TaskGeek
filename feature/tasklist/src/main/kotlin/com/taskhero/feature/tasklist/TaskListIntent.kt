package com.taskhero.feature.tasklist

import com.taskhero.core.parser.ParsedTaskData
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.model.SortOrder

/**
 * User intents for TaskList screen following MVI pattern.
 * Represents all possible user actions on the task list.
 */
sealed interface TaskListIntent {
    /**
     * Load or reload tasks from repository.
     */
    data object LoadTasks : TaskListIntent

    /**
     * Create a new task with description.
     *
     * @property description The task description
     */
    data class CreateTask(val description: String) : TaskListIntent

    /**
     * Create a task from quick entry with parsed natural language data.
     *
     * @property parsedData The parsed task data from natural language input
     */
    data class CreateQuickTask(val parsedData: ParsedTaskData) : TaskListIntent

    /**
     * Update an existing task.
     *
     * @property task The task to update
     */
    data class UpdateTask(val task: Task) : TaskListIntent

    /**
     * Delete a task by UUID.
     *
     * @property uuid The UUID of task to delete
     */
    data class DeleteTask(val uuid: String) : TaskListIntent

    /**
     * Mark a task as completed.
     *
     * @property uuid The UUID of task to complete
     */
    data class CompleteTask(val uuid: String) : TaskListIntent

    /**
     * Change the filter applied to tasks.
     *
     * @property filter The new filter to apply
     */
    data class FilterChanged(val filter: TaskFilter) : TaskListIntent

    /**
     * Change the sort order of tasks.
     *
     * @property sortOrder The new sort order
     */
    data class SortChanged(val sortOrder: SortOrder) : TaskListIntent

    /**
     * Open brain dump dialog for adding multiple tasks at once.
     */
    data object OpenBrainDump : TaskListIntent

    /**
     * Create multiple tasks from brain dump input.
     *
     * @property tasks List of parsed task data from brain dump
     */
    data class CreateMultipleTasks(val tasks: List<ParsedTaskData>) : TaskListIntent
}
