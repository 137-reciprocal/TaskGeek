package com.taskhero.feature.tasklist

import app.cash.turbine.test
import com.taskhero.domain.task.model.SortOrder
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.usecase.AddTaskUseCase
import com.taskhero.domain.task.usecase.CompleteTaskUseCase
import com.taskhero.domain.task.usecase.DeleteTaskUseCase
import com.taskhero.domain.task.usecase.GetTasksUseCase
import com.taskhero.domain.task.usecase.UpdateTaskUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for TaskListViewModel.
 *
 * Tests:
 * - Loading state management
 * - Success state with tasks
 * - Error state handling
 * - Intent handling (LoadTasks, CompleteTask, DeleteTask, etc.)
 * - Effect emission (ShowSnackbar)
 * - Filtering and sorting
 * - MVI pattern compliance
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private lateinit var viewModel: TaskListViewModel
    private lateinit var mockGetTasksUseCase: GetTasksUseCase
    private lateinit var mockAddTaskUseCase: AddTaskUseCase
    private lateinit var mockUpdateTaskUseCase: UpdateTaskUseCase
    private lateinit var mockCompleteTaskUseCase: CompleteTaskUseCase
    private lateinit var mockDeleteTaskUseCase: DeleteTaskUseCase

    private val testDispatcher = StandardTestDispatcher()
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockGetTasksUseCase = mockk()
        mockAddTaskUseCase = mockk()
        mockUpdateTaskUseCase = mockk()
        mockCompleteTaskUseCase = mockk()
        mockDeleteTaskUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TaskListViewModel {
        return TaskListViewModel(
            getTasksUseCase = mockGetTasksUseCase,
            addTaskUseCase = mockAddTaskUseCase,
            updateTaskUseCase = mockUpdateTaskUseCase,
            completeTaskUseCase = mockCompleteTaskUseCase,
            deleteTaskUseCase = mockDeleteTaskUseCase
        )
    }

    private fun createTask(
        uuid: String = "test-uuid",
        description: String = "Test task",
        status: TaskStatus = TaskStatus.PENDING,
        priority: TaskPriority? = null,
        urgency: Double = 5.0,
        project: String? = null
    ): Task {
        return Task(
            uuid = uuid,
            description = description,
            status = status,
            entry = currentTime,
            priority = priority,
            urgency = urgency,
            project = project
        )
    }

    @Test
    fun `test initial state is Loading`() = runTest {
        // Given
        every { mockGetTasksUseCase() } returns flowOf(emptyList())

        // When
        viewModel = createViewModel()

        // Then
        assertTrue("Initial state should be Loading", viewModel.uiState.value is TaskListUiState.Loading)
    }

    @Test
    fun `test LoadTasks intent transitions to Success state with tasks`() = runTest {
        // Given
        val tasks = listOf(
            createTask(uuid = "1", description = "Task 1"),
            createTask(uuid = "2", description = "Task 2")
        )
        every { mockGetTasksUseCase() } returns flowOf(tasks)

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is TaskListUiState.Success)
        assertEquals("Should have 2 tasks", 2, (state as TaskListUiState.Success).tasks.size)
    }

    @Test
    fun `test LoadTasks intent transitions to Error state on failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { mockGetTasksUseCase() } returns flow { throw Exception(errorMessage) }

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is TaskListUiState.Error)
        assertEquals("Error message should match", errorMessage, (state as TaskListUiState.Error).message)
    }

    @Test
    fun `test CompleteTask intent calls use case and emits success effect`() = runTest {
        // Given
        val task = createTask(uuid = "task-1")
        every { mockGetTasksUseCase() } returns flowOf(listOf(task))
        coEvery { mockCompleteTaskUseCase(task) } returns Result.success(Unit)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.effect.test {
            viewModel.onIntent(TaskListIntent.CompleteTask("task-1"))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val effect = awaitItem()
            assertTrue("Effect should be ShowSnackbar", effect is TaskListEffect.ShowSnackbar)
            assertTrue(
                "Message should indicate success",
                (effect as TaskListEffect.ShowSnackbar).message.contains("completed", ignoreCase = true)
            )
        }

        coVerify(exactly = 1) { mockCompleteTaskUseCase(task) }
    }

    @Test
    fun `test CompleteTask intent emits error effect on failure`() = runTest {
        // Given
        val task = createTask(uuid = "task-1")
        val errorMessage = "Failed to complete"
        every { mockGetTasksUseCase() } returns flowOf(listOf(task))
        coEvery { mockCompleteTaskUseCase(task) } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.effect.test {
            viewModel.onIntent(TaskListIntent.CompleteTask("task-1"))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val effect = awaitItem()
            assertTrue("Effect should be ShowSnackbar", effect is TaskListEffect.ShowSnackbar)
            assertTrue(
                "Message should contain error",
                (effect as TaskListEffect.ShowSnackbar).message.contains("Failed", ignoreCase = true)
            )
        }
    }

    @Test
    fun `test DeleteTask intent calls use case and emits success effect`() = runTest {
        // Given
        every { mockGetTasksUseCase() } returns flowOf(emptyList())
        coEvery { mockDeleteTaskUseCase("task-1") } returns Result.success(Unit)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.effect.test {
            viewModel.onIntent(TaskListIntent.DeleteTask("task-1"))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val effect = awaitItem()
            assertTrue("Effect should be ShowSnackbar", effect is TaskListEffect.ShowSnackbar)
            assertTrue(
                "Message should indicate deletion",
                (effect as TaskListEffect.ShowSnackbar).message.contains("deleted", ignoreCase = true)
            )
        }

        coVerify(exactly = 1) { mockDeleteTaskUseCase("task-1") }
    }

    @Test
    fun `test DeleteTask intent emits error effect on failure`() = runTest {
        // Given
        val errorMessage = "Cannot delete task"
        every { mockGetTasksUseCase() } returns flowOf(emptyList())
        coEvery { mockDeleteTaskUseCase("task-1") } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.effect.test {
            viewModel.onIntent(TaskListIntent.DeleteTask("task-1"))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val effect = awaitItem()
            assertTrue("Effect should be ShowSnackbar", effect is TaskListEffect.ShowSnackbar)
            assertTrue(
                "Message should contain error",
                (effect as TaskListEffect.ShowSnackbar).message.contains("Failed", ignoreCase = true)
            )
        }
    }

    @Test
    fun `test CreateTask intent creates task and emits success effect`() = runTest {
        // Given
        val description = "New task"
        every { mockGetTasksUseCase() } returns flowOf(emptyList())
        coEvery { mockAddTaskUseCase(any()) } returns Result.success(Unit)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.effect.test {
            viewModel.onIntent(TaskListIntent.CreateTask(description))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val effect = awaitItem()
            assertTrue("Effect should be ShowSnackbar", effect is TaskListEffect.ShowSnackbar)
            assertTrue(
                "Message should indicate creation",
                (effect as TaskListEffect.ShowSnackbar).message.contains("created", ignoreCase = true)
            )
        }

        coVerify(exactly = 1) { mockAddTaskUseCase(any()) }
    }

    @Test
    fun `test UpdateTask intent updates task and emits success effect`() = runTest {
        // Given
        val task = createTask()
        every { mockGetTasksUseCase() } returns flowOf(emptyList())
        coEvery { mockUpdateTaskUseCase(any()) } returns Result.success(Unit)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.effect.test {
            viewModel.onIntent(TaskListIntent.UpdateTask(task))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val effect = awaitItem()
            assertTrue("Effect should be ShowSnackbar", effect is TaskListEffect.ShowSnackbar)
            assertTrue(
                "Message should indicate update",
                (effect as TaskListEffect.ShowSnackbar).message.contains("updated", ignoreCase = true)
            )
        }

        coVerify(exactly = 1) { mockUpdateTaskUseCase(any()) }
    }

    @Test
    fun `test FilterChanged intent updates state with filtered tasks`() = runTest {
        // Given
        val tasks = listOf(
            createTask(uuid = "1", status = TaskStatus.PENDING),
            createTask(uuid = "2", status = TaskStatus.COMPLETED),
            createTask(uuid = "3", status = TaskStatus.PENDING)
        )
        every { mockGetTasksUseCase() } returns flowOf(tasks)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onIntent(TaskListIntent.FilterChanged(TaskFilter(status = TaskStatus.PENDING)))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals("Should have 2 pending tasks", 2, state.tasks.size)
        assertTrue("All tasks should be pending", state.tasks.all { it.status == TaskStatus.PENDING })
    }

    @Test
    fun `test SortChanged intent updates state with sorted tasks`() = runTest {
        // Given
        val tasks = listOf(
            createTask(uuid = "1", urgency = 5.0),
            createTask(uuid = "2", urgency = 10.0),
            createTask(uuid = "3", urgency = 3.0)
        )
        every { mockGetTasksUseCase() } returns flowOf(tasks)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onIntent(TaskListIntent.SortChanged(SortOrder.URGENCY))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals("First task should have highest urgency", 10.0, state.tasks[0].urgency, 0.01)
        assertEquals("Last task should have lowest urgency", 3.0, state.tasks[2].urgency, 0.01)
    }

    @Test
    fun `test tasks are sorted by urgency by default`() = runTest {
        // Given
        val tasks = listOf(
            createTask(uuid = "1", urgency = 5.0),
            createTask(uuid = "2", urgency = 15.0),
            createTask(uuid = "3", urgency = 8.0)
        )
        every { mockGetTasksUseCase() } returns flowOf(tasks)

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals("First task should have urgency 15.0", 15.0, state.tasks[0].urgency, 0.01)
        assertEquals("Second task should have urgency 8.0", 8.0, state.tasks[1].urgency, 0.01)
        assertEquals("Third task should have urgency 5.0", 5.0, state.tasks[2].urgency, 0.01)
    }

    @Test
    fun `test filter by priority works correctly`() = runTest {
        // Given
        val tasks = listOf(
            createTask(uuid = "1", priority = TaskPriority.HIGH),
            createTask(uuid = "2", priority = TaskPriority.LOW),
            createTask(uuid = "3", priority = TaskPriority.HIGH)
        )
        every { mockGetTasksUseCase() } returns flowOf(tasks)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onIntent(TaskListIntent.FilterChanged(TaskFilter(priority = TaskPriority.HIGH)))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals("Should have 2 high priority tasks", 2, state.tasks.size)
        assertTrue("All tasks should be high priority", state.tasks.all { it.priority == TaskPriority.HIGH })
    }

    @Test
    fun `test filter by project works correctly`() = runTest {
        // Given
        val tasks = listOf(
            createTask(uuid = "1", project = "work"),
            createTask(uuid = "2", project = "home"),
            createTask(uuid = "3", project = "work")
        )
        every { mockGetTasksUseCase() } returns flowOf(tasks)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onIntent(TaskListIntent.FilterChanged(TaskFilter(project = "work")))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals("Should have 2 work tasks", 2, state.tasks.size)
        assertTrue("All tasks should be work project", state.tasks.all { it.project == "work" })
    }

    @Test
    fun `test empty task list shows success state`() = runTest {
        // Given
        every { mockGetTasksUseCase() } returns flowOf(emptyList())

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is TaskListUiState.Success)
        assertEquals("Task list should be empty", 0, (state as TaskListUiState.Success).tasks.size)
    }

    @Test
    fun `test LoadTasks intent reloads tasks`() = runTest {
        // Given
        every { mockGetTasksUseCase() } returns flowOf(emptyList())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onIntent(TaskListIntent.LoadTasks)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(atLeast = 2) { mockGetTasksUseCase() }
    }

    @Test
    fun `test CompleteTask with non-existent task does not emit effect`() = runTest {
        // Given
        every { mockGetTasksUseCase() } returns flowOf(emptyList())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.effect.test {
            viewModel.onIntent(TaskListIntent.CompleteTask("non-existent-uuid"))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then - no effect should be emitted
            expectNoEvents()
        }

        coVerify(exactly = 0) { mockCompleteTaskUseCase(any()) }
    }

    @Test
    fun `test multiple filters can be combined`() = runTest {
        // Given
        val tasks = listOf(
            createTask(uuid = "1", status = TaskStatus.PENDING, priority = TaskPriority.HIGH),
            createTask(uuid = "2", status = TaskStatus.COMPLETED, priority = TaskPriority.HIGH),
            createTask(uuid = "3", status = TaskStatus.PENDING, priority = TaskPriority.LOW)
        )
        every { mockGetTasksUseCase() } returns flowOf(tasks)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onIntent(
            TaskListIntent.FilterChanged(
                TaskFilter(status = TaskStatus.PENDING, priority = TaskPriority.HIGH)
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as TaskListUiState.Success
        assertEquals("Should have 1 task matching both filters", 1, state.tasks.size)
        assertEquals("Task should be uuid 1", "1", state.tasks[0].uuid)
    }
}
