package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to detect circular dependencies in task dependencies.
 * Uses depth-first search to detect cycles in the dependency graph.
 */
class DetectDependencyCycleUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    /**
     * Detect if adding a dependency would create a cycle.
     *
     * @param taskUuid UUID of the task that would depend on another
     * @param dependsOnUuid UUID of the task that would be depended upon
     * @return Result containing true if cycle detected, false otherwise
     */
    suspend operator fun invoke(taskUuid: String, dependsOnUuid: String): Result<Boolean> {
        return try {
            // If a task depends on itself, that's a cycle
            if (taskUuid == dependsOnUuid) {
                return Result.success(true)
            }

            // Get all tasks to build dependency graph
            val allTasks = repository.getAllTasks().first()
            val taskMap = allTasks.associateBy { it.uuid }

            // Check if the proposed dependency would create a cycle
            // This is true if there's already a path from dependsOnUuid to taskUuid
            val hasCycle = hasPath(dependsOnUuid, taskUuid, taskMap)

            Result.success(hasCycle)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if there's a path from start to target in the dependency graph.
     * Uses depth-first search with cycle detection.
     *
     * @param start Starting task UUID
     * @param target Target task UUID
     * @param taskMap Map of all tasks by UUID
     * @return true if path exists, false otherwise
     */
    private fun hasPath(
        start: String,
        target: String,
        taskMap: Map<String, Task>
    ): Boolean {
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()

        return dfs(start, target, taskMap, visited, recursionStack)
    }

    /**
     * Depth-first search to find path from current to target.
     *
     * @param current Current task UUID
     * @param target Target task UUID
     * @param taskMap Map of all tasks by UUID
     * @param visited Set of visited task UUIDs
     * @param recursionStack Set of tasks in current recursion stack (for cycle detection)
     * @return true if path exists, false otherwise
     */
    private fun dfs(
        current: String,
        target: String,
        taskMap: Map<String, Task>,
        visited: MutableSet<String>,
        recursionStack: MutableSet<String>
    ): Boolean {
        // If we've reached the target, we found a path
        if (current == target) {
            return true
        }

        // If we've already visited this node, skip it
        if (visited.contains(current)) {
            return false
        }

        // Mark as visited and add to recursion stack
        visited.add(current)
        recursionStack.add(current)

        // Get the current task
        val currentTask = taskMap[current]

        // Check all dependencies of the current task
        if (currentTask != null) {
            for (dependencyUuid in currentTask.dependencies) {
                // If dependency is in recursion stack, we have a cycle
                if (recursionStack.contains(dependencyUuid)) {
                    return true
                }

                // Recursively check if there's a path from dependency to target
                if (dfs(dependencyUuid, target, taskMap, visited, recursionStack)) {
                    return true
                }
            }
        }

        // Remove from recursion stack when backtracking
        recursionStack.remove(current)

        return false
    }
}
