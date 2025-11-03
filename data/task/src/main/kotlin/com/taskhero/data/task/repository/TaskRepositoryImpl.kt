package com.taskhero.data.task.repository

import com.taskhero.core.database.dao.AnnotationDao
import com.taskhero.core.database.dao.TagDao
import com.taskhero.core.database.dao.TaskDao
import com.taskhero.core.database.dao.TaskDependencyDao
import com.taskhero.core.database.dao.TaskTagDao
import com.taskhero.core.database.entity.AnnotationEntity
import com.taskhero.core.database.entity.TagEntity
import com.taskhero.core.database.entity.TaskDependencyCrossRef
import com.taskhero.core.database.entity.TaskTagCrossRef
import com.taskhero.data.task.mapper.AnnotationMapper.toDomain
import com.taskhero.data.task.mapper.AnnotationMapper.toEntity
import com.taskhero.data.task.mapper.TaskMapper.toDomain
import com.taskhero.data.task.mapper.TaskMapper.toEntity
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TaskRepository that manages task data using Room database.
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val annotationDao: AnnotationDao,
    private val tagDao: TagDao,
    private val taskTagDao: TaskTagDao,
    private val taskDependencyDao: TaskDependencyDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { taskEntities ->
            taskEntities.map { taskEntity ->
                combineTaskWithRelations(taskEntity.uuid)
            }
        }.map { flows ->
            // Combine all individual task flows into a single list
            val tasks = mutableListOf<Task>()
            flows.forEach { flow ->
                flow.collect { task ->
                    task?.let { tasks.add(it) }
                }
            }
            tasks
        }
    }

    override fun getTaskByUuid(uuid: String): Flow<Task?> {
        return combineTaskWithRelations(uuid)
    }

    override fun getNextTasks(): Flow<List<Task>> {
        return taskDao.getNextTasks().map { taskEntities ->
            taskEntities.map { taskEntity ->
                combineTaskWithRelations(taskEntity.uuid)
            }
        }.map { flows ->
            // Combine all individual task flows into a single list
            val tasks = mutableListOf<Task>()
            flows.forEach { flow ->
                flow.collect { task ->
                    task?.let { tasks.add(it) }
                }
            }
            tasks
        }
    }

    override suspend fun insertTask(task: Task): Result<Unit> {
        return try {
            // Insert the task entity
            taskDao.insert(task.toEntity())

            // Insert tags and create cross-references
            task.tags.forEach { tagName ->
                tagDao.insert(TagEntity(tagName))
                taskTagDao.addTagToTask(TaskTagCrossRef(task.uuid, tagName))
            }

            // Insert annotations
            task.annotations.forEach { annotation ->
                annotationDao.insert(annotation.toEntity())
            }

            // Insert dependencies
            task.dependencies.forEach { dependsOnUuid ->
                taskDependencyDao.addDependency(
                    TaskDependencyCrossRef(task.uuid, dependsOnUuid)
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            // Update the task entity
            taskDao.update(task.toEntity())

            // Update tags - delete existing and insert new ones
            taskTagDao.deleteAllTagsForTask(task.uuid)
            task.tags.forEach { tagName ->
                tagDao.insert(TagEntity(tagName))
                taskTagDao.addTagToTask(TaskTagCrossRef(task.uuid, tagName))
            }

            // Update annotations - for now, we'll keep the existing logic
            // In a production app, you might want to handle updates more granularly
            // Note: Annotations with cascade delete will be removed if task is deleted

            // Update dependencies
            taskDependencyDao.deleteAllDependenciesForTask(task.uuid)
            task.dependencies.forEach { dependsOnUuid ->
                taskDependencyDao.addDependency(
                    TaskDependencyCrossRef(task.uuid, dependsOnUuid)
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(uuid: String): Result<Unit> {
        return try {
            taskDao.deleteByUuid(uuid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTasksByProject(projectName: String): Flow<List<Task>> {
        return taskDao.getTasksByProject(projectName).map { taskEntities ->
            taskEntities.map { taskEntity ->
                combineTaskWithRelations(taskEntity.uuid)
            }
        }.map { flows ->
            // Combine all individual task flows into a single list
            val tasks = mutableListOf<Task>()
            flows.forEach { flow ->
                flow.collect { task ->
                    task?.let { tasks.add(it) }
                }
            }
            tasks
        }
    }

    override fun getTasksByStatus(status: String): Flow<List<Task>> {
        // Convert string to TaskStatus enum
        val taskStatus = try {
            TaskStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            TaskStatus.PENDING // Default fallback
        }

        return taskDao.getTasksByStatus(taskStatus).map { taskEntities ->
            taskEntities.map { taskEntity ->
                combineTaskWithRelations(taskEntity.uuid)
            }
        }.map { flows ->
            // Combine all individual task flows into a single list
            val tasks = mutableListOf<Task>()
            flows.forEach { flow ->
                flow.collect { task ->
                    task?.let { tasks.add(it) }
                }
            }
            tasks
        }
    }

    /**
     * Helper function to combine a task entity with its related data (tags, annotations, dependencies).
     */
    private fun combineTaskWithRelations(uuid: String): Flow<Task?> {
        return combine(
            taskDao.getTaskByUuid(uuid),
            taskTagDao.getTagsForTask(uuid),
            annotationDao.getAnnotationsForTask(uuid),
            taskDependencyDao.getDependenciesForTask(uuid)
        ) { taskEntity, tags, annotationEntities, dependencies ->
            taskEntity?.toDomain(
                tags = tags,
                annotations = annotationEntities.map { it.toDomain() },
                dependencies = dependencies
            )
        }
    }
}
