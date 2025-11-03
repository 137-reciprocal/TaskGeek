package com.taskhero.widget

import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.repository.HeroRepository
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for widget data operations.
 * Provides simplified data access for widgets without reactive streams.
 */
@Singleton
class WidgetRepository @Inject constructor(
    private val taskRepository: TaskRepository,
    private val heroRepository: HeroRepository
) {
    /**
     * Get next tasks for widget display, sorted by urgency.
     * Returns a snapshot of tasks (non-reactive).
     *
     * @param limit Maximum number of tasks to return
     * @return List of tasks sorted by urgency (highest first)
     */
    suspend fun getNextTasksForWidget(limit: Int): List<Task> {
        return taskRepository.getNextTasks()
            .firstOrNull()
            ?.sortedByDescending { it.urgency }
            ?.take(limit)
            ?: emptyList()
    }

    /**
     * Get hero for widget display.
     * Returns a snapshot of the hero (non-reactive).
     *
     * @return Hero object or null if not found
     */
    suspend fun getHeroForWidget(): Hero? {
        return heroRepository.getHero().firstOrNull()
    }
}
