package com.taskhero.domain.filter.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskFilter
import javax.inject.Inject

/**
 * Use case for applying a filter to a list of tasks.
 * Filters tasks based on various criteria like status, priority, project, tags, etc.
 */
class ApplyFilterUseCase @Inject constructor() {

    /**
     * Apply the filter to a list of tasks.
     *
     * @param tasks The list of tasks to filter
     * @param filter The filter criteria to apply
     * @return Filtered list of tasks
     */
    operator fun invoke(tasks: List<Task>, filter: TaskFilter): List<Task> {
        var filtered = tasks

        // Filter by UUID
        filter.uuid?.let { uuid ->
            filtered = filtered.filter { it.uuid == uuid }
        }

        // Filter by description (case-insensitive partial match)
        filter.description?.let { description ->
            filtered = filtered.filter {
                it.description.contains(description, ignoreCase = true)
            }
        }

        // Filter by status
        filter.status?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        // Filter by statuses (multiple)
        if (filter.statuses.isNotEmpty()) {
            filtered = filtered.filter { task ->
                filter.statuses.contains(task.status)
            }
        }

        // Filter by project
        filter.project?.let { project ->
            filtered = filtered.filter { it.project == project }
        }

        // Filter by priority
        filter.priority?.let { priority ->
            filtered = filtered.filter { it.priority == priority }
        }

        // Filter by tags (task must have at least one of the filter tags)
        if (filter.tags.isNotEmpty()) {
            filtered = filtered.filter { task ->
                filter.tags.any { tag -> task.tags.contains(tag) }
            }
        }

        // Filter by dependencies
        if (filter.dependencies.isNotEmpty()) {
            filtered = filtered.filter { task ->
                filter.dependencies.any { dep -> task.dependencies.contains(dep) }
            }
        }

        // Filter by entry date range
        filter.entryStarting?.let { start ->
            filtered = filtered.filter { it.entry >= start }
        }
        filter.entryUntil?.let { until ->
            filtered = filtered.filter { it.entry <= until }
        }

        // Filter by modified date
        filter.modified?.let { modified ->
            filtered = filtered.filter { task ->
                (task.modified ?: task.entry) >= modified
            }
        }

        // Filter by start date
        filter.start?.let { start ->
            filtered = filtered.filter { task ->
                task.start?.let { it >= start } ?: false
            }
        }

        // Filter by end date
        filter.end?.let { end ->
            filtered = filtered.filter { task ->
                task.end?.let { it <= end } ?: false
            }
        }

        // Filter by due date
        filter.due?.let { due ->
            filtered = filtered.filter { task ->
                task.due?.let { it <= due } ?: false
            }
        }

        // Filter by wait date
        filter.wait?.let { wait ->
            filtered = filtered.filter { task ->
                task.wait?.let { it <= wait } ?: false
            }
        }

        // Filter by scheduled date
        filter.scheduled?.let { scheduled ->
            filtered = filtered.filter { task ->
                task.scheduled?.let { it <= scheduled } ?: false
            }
        }

        // Filter by until date
        filter.until?.let { until ->
            filtered = filtered.filter { task ->
                task.until?.let { it <= until } ?: false
            }
        }

        // Filter by recurrence
        filter.recur?.let { recur ->
            filtered = filtered.filter { it.recur == recur }
        }

        // Filter by parent
        filter.parent?.let { parent ->
            filtered = filtered.filter { it.parent == parent }
        }

        // Filter by urgency range
        filter.urgencyMin?.let { min ->
            filtered = filtered.filter { it.urgency >= min }
        }
        filter.urgencyMax?.let { max ->
            filtered = filtered.filter { it.urgency <= max }
        }

        return filtered
    }
}
