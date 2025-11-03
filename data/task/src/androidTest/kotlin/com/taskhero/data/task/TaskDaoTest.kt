package com.taskhero.data.task

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.taskhero.core.database.TaskHeroDatabase
import com.taskhero.core.database.dao.TaskDao
import com.taskhero.core.database.entity.TaskEntity
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for TaskDao using in-memory database.
 *
 * Tests:
 * - Insert task
 * - Update task
 * - Delete task
 * - Query by UUID
 * - Query by status
 * - Query next tasks (with wait filter)
 * - Query by project
 * - Complex queries with filters
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: TaskHeroDatabase
    private lateinit var taskDao: TaskDao
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            TaskHeroDatabase::class.java
        ).allowMainThreadQueries().build()

        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTaskEntity(
        uuid: String = "test-uuid",
        description: String = "Test task",
        status: TaskStatus = TaskStatus.PENDING,
        priority: TaskPriority? = null,
        urgency: Double = 5.0,
        project: String? = null,
        due: Long? = null,
        wait: Long? = null
    ): TaskEntity {
        return TaskEntity(
            uuid = uuid,
            description = description,
            status = status,
            entry = currentTime,
            priority = priority,
            urgency = urgency,
            project = project,
            due = due,
            wait = wait
        )
    }

    @Test
    fun testInsertTask() = runTest {
        // Given
        val task = createTaskEntity(uuid = "insert-test-1")

        // When
        taskDao.insert(task)

        // Then
        val retrieved = taskDao.getTaskByUuid("insert-test-1").first()
        assertNotNull("Task should be inserted", retrieved)
        assertEquals("UUID should match", "insert-test-1", retrieved?.uuid)
        assertEquals("Description should match", "Test task", retrieved?.description)
    }

    @Test
    fun testInsertMultipleTasks() = runTest {
        // Given
        val task1 = createTaskEntity(uuid = "task-1", description = "Task 1")
        val task2 = createTaskEntity(uuid = "task-2", description = "Task 2")
        val task3 = createTaskEntity(uuid = "task-3", description = "Task 3")

        // When
        taskDao.insert(task1)
        taskDao.insert(task2)
        taskDao.insert(task3)

        // Then
        val allTasks = taskDao.getAllTasks().first()
        assertEquals("Should have 3 tasks", 3, allTasks.size)
    }

    @Test
    fun testUpdateTask() = runTest {
        // Given
        val task = createTaskEntity(uuid = "update-test", description = "Original")
        taskDao.insert(task)

        // When
        val updatedTask = task.copy(description = "Updated", modified = currentTime)
        taskDao.update(updatedTask)

        // Then
        val retrieved = taskDao.getTaskByUuid("update-test").first()
        assertEquals("Description should be updated", "Updated", retrieved?.description)
        assertEquals("Modified timestamp should be set", currentTime, retrieved?.modified)
    }

    @Test
    fun testDeleteTask() = runTest {
        // Given
        val task = createTaskEntity(uuid = "delete-test")
        taskDao.insert(task)

        // When
        taskDao.delete(task)

        // Then
        val retrieved = taskDao.getTaskByUuid("delete-test").first()
        assertNull("Task should be deleted", retrieved)
    }

    @Test
    fun testDeleteByUuid() = runTest {
        // Given
        val task = createTaskEntity(uuid = "delete-by-uuid-test")
        taskDao.insert(task)

        // When
        taskDao.deleteByUuid("delete-by-uuid-test")

        // Then
        val retrieved = taskDao.getTaskByUuid("delete-by-uuid-test").first()
        assertNull("Task should be deleted", retrieved)
    }

    @Test
    fun testGetTaskByUuid() = runTest {
        // Given
        val task = createTaskEntity(uuid = "get-by-uuid-test", description = "Specific task")
        taskDao.insert(task)

        // When
        val retrieved = taskDao.getTaskByUuid("get-by-uuid-test").first()

        // Then
        assertNotNull("Task should be found", retrieved)
        assertEquals("UUID should match", "get-by-uuid-test", retrieved?.uuid)
        assertEquals("Description should match", "Specific task", retrieved?.description)
    }

    @Test
    fun testGetTaskByUuidNotFound() = runTest {
        // When
        val retrieved = taskDao.getTaskByUuid("non-existent").first()

        // Then
        assertNull("Non-existent task should return null", retrieved)
    }

    @Test
    fun testGetTasksByStatus() = runTest {
        // Given
        val pendingTask1 = createTaskEntity(uuid = "pending-1", status = TaskStatus.PENDING)
        val pendingTask2 = createTaskEntity(uuid = "pending-2", status = TaskStatus.PENDING)
        val completedTask = createTaskEntity(uuid = "completed-1", status = TaskStatus.COMPLETED)

        taskDao.insert(pendingTask1)
        taskDao.insert(pendingTask2)
        taskDao.insert(completedTask)

        // When
        val pendingTasks = taskDao.getTasksByStatus(TaskStatus.PENDING).first()

        // Then
        assertEquals("Should have 2 pending tasks", 2, pendingTasks.size)
        assertTrue("All tasks should be pending", pendingTasks.all { it.status == TaskStatus.PENDING })
    }

    @Test
    fun testGetTasksByStatusSortedByUrgency() = runTest {
        // Given
        val task1 = createTaskEntity(uuid = "task-1", status = TaskStatus.PENDING, urgency = 5.0)
        val task2 = createTaskEntity(uuid = "task-2", status = TaskStatus.PENDING, urgency = 15.0)
        val task3 = createTaskEntity(uuid = "task-3", status = TaskStatus.PENDING, urgency = 10.0)

        taskDao.insert(task1)
        taskDao.insert(task2)
        taskDao.insert(task3)

        // When
        val tasks = taskDao.getTasksByStatus(TaskStatus.PENDING).first()

        // Then
        assertEquals("First task should have highest urgency", 15.0, tasks[0].urgency, 0.01)
        assertEquals("Second task should have urgency 10.0", 10.0, tasks[1].urgency, 0.01)
        assertEquals("Third task should have lowest urgency", 5.0, tasks[2].urgency, 0.01)
    }

    @Test
    fun testGetNextTasks() = runTest {
        // Given
        val futureTime = currentTime + 1000000L
        val pastTime = currentTime - 1000L

        val availableTask1 = createTaskEntity(uuid = "available-1", wait = null)
        val availableTask2 = createTaskEntity(uuid = "available-2", wait = pastTime)
        val waitingTask = createTaskEntity(uuid = "waiting-1", wait = futureTime)

        taskDao.insert(availableTask1)
        taskDao.insert(availableTask2)
        taskDao.insert(waitingTask)

        // When
        val nextTasks = taskDao.getNextTasks(currentTime).first()

        // Then
        assertEquals("Should have 2 available tasks", 2, nextTasks.size)
        assertTrue("Should not include waiting task", nextTasks.none { it.uuid == "waiting-1" })
    }

    @Test
    fun testGetNextTasksLimitTo50() = runTest {
        // Given - Insert 60 tasks
        repeat(60) { index ->
            val task = createTaskEntity(
                uuid = "task-$index",
                status = TaskStatus.PENDING,
                urgency = index.toDouble()
            )
            taskDao.insert(task)
        }

        // When
        val nextTasks = taskDao.getNextTasks().first()

        // Then
        assertTrue("Should return at most 50 tasks", nextTasks.size <= 50)
    }

    @Test
    fun testGetTasksByProject() = runTest {
        // Given
        val workTask1 = createTaskEntity(uuid = "work-1", project = "work")
        val workTask2 = createTaskEntity(uuid = "work-2", project = "work.mobile")
        val homeTask = createTaskEntity(uuid = "home-1", project = "home")

        taskDao.insert(workTask1)
        taskDao.insert(workTask2)
        taskDao.insert(homeTask)

        // When
        val workTasks = taskDao.getTasksByProject("work").first()

        // Then
        assertEquals("Should have 2 work-related tasks", 2, workTasks.size)
        assertTrue("All tasks should start with 'work'", workTasks.all { it.project?.startsWith("work") == true })
    }

    @Test
    fun testGetTasksByProjectPrefix() = runTest {
        // Given
        val parentProject = createTaskEntity(uuid = "parent", project = "personal")
        val childProject1 = createTaskEntity(uuid = "child1", project = "personal.health")
        val childProject2 = createTaskEntity(uuid = "child2", project = "personal.finance")
        val unrelatedProject = createTaskEntity(uuid = "unrelated", project = "work")

        taskDao.insert(parentProject)
        taskDao.insert(childProject1)
        taskDao.insert(childProject2)
        taskDao.insert(unrelatedProject)

        // When
        val personalTasks = taskDao.getTasksByProject("personal").first()

        // Then
        assertEquals("Should have 3 personal-related tasks", 3, personalTasks.size)
        assertTrue("Should include parent project", personalTasks.any { it.uuid == "parent" })
        assertTrue("Should include child projects", personalTasks.any { it.uuid == "child1" })
    }

    @Test
    fun testGetAllTasks() = runTest {
        // Given
        val task1 = createTaskEntity(uuid = "all-1")
        val task2 = createTaskEntity(uuid = "all-2")
        val task3 = createTaskEntity(uuid = "all-3")

        taskDao.insert(task1)
        taskDao.insert(task2)
        taskDao.insert(task3)

        // When
        val allTasks = taskDao.getAllTasks().first()

        // Then
        assertEquals("Should have 3 tasks", 3, allTasks.size)
    }

    @Test
    fun testReplaceOnConflict() = runTest {
        // Given
        val task = createTaskEntity(uuid = "conflict-test", description = "Original")
        taskDao.insert(task)

        // When - Insert same UUID with different description
        val updatedTask = task.copy(description = "Replaced")
        taskDao.insert(updatedTask)

        // Then
        val retrieved = taskDao.getTaskByUuid("conflict-test").first()
        assertEquals("Description should be replaced", "Replaced", retrieved?.description)

        val allTasks = taskDao.getAllTasks().first()
        assertEquals("Should still have only 1 task", 1, allTasks.size)
    }

    @Test
    fun testComplexQueryWithMultipleFilters() = runTest {
        // Given
        val highPriorityPending1 = createTaskEntity(
            uuid = "hp-pending-1",
            status = TaskStatus.PENDING,
            priority = TaskPriority.HIGH,
            urgency = 20.0
        )
        val highPriorityPending2 = createTaskEntity(
            uuid = "hp-pending-2",
            status = TaskStatus.PENDING,
            priority = TaskPriority.HIGH,
            urgency = 18.0
        )
        val highPriorityCompleted = createTaskEntity(
            uuid = "hp-completed",
            status = TaskStatus.COMPLETED,
            priority = TaskPriority.HIGH,
            urgency = 15.0
        )
        val lowPriorityPending = createTaskEntity(
            uuid = "lp-pending",
            status = TaskStatus.PENDING,
            priority = TaskPriority.LOW,
            urgency = 5.0
        )

        taskDao.insert(highPriorityPending1)
        taskDao.insert(highPriorityPending2)
        taskDao.insert(highPriorityCompleted)
        taskDao.insert(lowPriorityPending)

        // When
        val pendingTasks = taskDao.getTasksByStatus(TaskStatus.PENDING).first()

        // Then
        val highPriorityPendingTasks = pendingTasks.filter { it.priority == TaskPriority.HIGH }
        assertEquals("Should have 2 high priority pending tasks", 2, highPriorityPendingTasks.size)
        assertEquals("First should be highest urgency", 20.0, highPriorityPendingTasks[0].urgency, 0.01)
    }

    @Test
    fun testEmptyDatabase() = runTest {
        // When
        val allTasks = taskDao.getAllTasks().first()

        // Then
        assertTrue("Empty database should return empty list", allTasks.isEmpty())
    }

    @Test
    fun testTaskWithAllFieldsSet() = runTest {
        // Given
        val fullTask = TaskEntity(
            uuid = "full-task",
            description = "Complete task",
            status = TaskStatus.PENDING,
            entry = currentTime,
            modified = currentTime + 1000,
            start = currentTime + 2000,
            end = null,
            due = currentTime + 10000,
            wait = currentTime + 5000,
            scheduled = currentTime + 3000,
            until = currentTime + 20000,
            project = "test.project",
            priority = TaskPriority.MEDIUM,
            recur = "weekly",
            parent = "parent-uuid",
            imask = 1,
            mask = "mask-uuid",
            urgency = 12.5
        )

        // When
        taskDao.insert(fullTask)

        // Then
        val retrieved = taskDao.getTaskByUuid("full-task").first()
        assertNotNull("Task should be inserted", retrieved)
        assertEquals("All fields should be preserved", fullTask, retrieved)
    }

    @Test
    fun testUpdateStatusAndTimestamps() = runTest {
        // Given
        val task = createTaskEntity(uuid = "status-test", status = TaskStatus.PENDING)
        taskDao.insert(task)

        // When
        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            end = currentTime + 5000,
            modified = currentTime + 5000
        )
        taskDao.update(completedTask)

        // Then
        val retrieved = taskDao.getTaskByUuid("status-test").first()
        assertEquals("Status should be completed", TaskStatus.COMPLETED, retrieved?.status)
        assertEquals("End timestamp should be set", currentTime + 5000, retrieved?.end)
        assertEquals("Modified timestamp should be updated", currentTime + 5000, retrieved?.modified)
    }

    @Test
    fun testQueryNextTasksFiltersByStatusPending() = runTest {
        // Given
        val pendingTask = createTaskEntity(uuid = "pending", status = TaskStatus.PENDING)
        val completedTask = createTaskEntity(uuid = "completed", status = TaskStatus.COMPLETED)
        val deletedTask = createTaskEntity(uuid = "deleted", status = TaskStatus.DELETED)

        taskDao.insert(pendingTask)
        taskDao.insert(completedTask)
        taskDao.insert(deletedTask)

        // When
        val nextTasks = taskDao.getNextTasks().first()

        // Then
        assertEquals("Should only return pending tasks", 1, nextTasks.size)
        assertEquals("Should return pending task", "pending", nextTasks[0].uuid)
    }

    @Test
    fun testMultipleDeletions() = runTest {
        // Given
        val task1 = createTaskEntity(uuid = "delete-1")
        val task2 = createTaskEntity(uuid = "delete-2")
        val task3 = createTaskEntity(uuid = "delete-3")

        taskDao.insert(task1)
        taskDao.insert(task2)
        taskDao.insert(task3)

        // When
        taskDao.deleteByUuid("delete-1")
        taskDao.deleteByUuid("delete-3")

        // Then
        val remaining = taskDao.getAllTasks().first()
        assertEquals("Should have 1 task remaining", 1, remaining.size)
        assertEquals("Should be task 2", "delete-2", remaining[0].uuid)
    }
}
