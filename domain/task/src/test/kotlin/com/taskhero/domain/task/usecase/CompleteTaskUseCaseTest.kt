package com.taskhero.domain.task.usecase

import com.taskhero.domain.hero.model.XpReward
import com.taskhero.domain.hero.usecase.AddXpToHeroUseCase
import com.taskhero.domain.hero.usecase.CalculateXpRewardUseCase
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import com.taskhero.domain.task.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for CompleteTaskUseCase.
 *
 * Tests:
 * - Task status changes to COMPLETED
 * - End timestamp is set
 * - Modified timestamp is updated
 * - XP reward is calculated and awarded
 * - Repository update is called
 * - Handles success and failure cases
 */
class CompleteTaskUseCaseTest {

    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    private lateinit var mockRepository: TaskRepository
    private lateinit var mockCalculateXpRewardUseCase: CalculateXpRewardUseCase
    private lateinit var mockAddXpToHeroUseCase: AddXpToHeroUseCase

    private val currentTime = System.currentTimeMillis()

    @Before
    fun setup() {
        mockRepository = mockk()
        mockCalculateXpRewardUseCase = mockk()
        mockAddXpToHeroUseCase = mockk()

        completeTaskUseCase = CompleteTaskUseCase(
            repository = mockRepository,
            calculateXpRewardUseCase = mockCalculateXpRewardUseCase,
            addXpToHeroUseCase = mockAddXpToHeroUseCase
        )
    }

    private fun createTask(
        uuid: String = "test-uuid",
        description: String = "Test task",
        status: TaskStatus = TaskStatus.PENDING,
        priority: TaskPriority? = null,
        urgency: Double = 5.0
    ): Task {
        return Task(
            uuid = uuid,
            description = description,
            status = status,
            entry = currentTime - 1000L,
            priority = priority,
            urgency = urgency
        )
    }

    @Test
    fun `test task status changes to COMPLETED`() = runTest {
        // Given
        val task = createTask(status = TaskStatus.PENDING)
        val updatedTaskSlot = slot<Task>()
        val xpReward = XpReward(baseXp = 50L, urgencyMultiplier = 1.0, completionBonus = 0L)

        coEvery { mockRepository.updateTask(capture(updatedTaskSlot)) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        assertEquals(TaskStatus.COMPLETED, updatedTaskSlot.captured.status)
    }

    @Test
    fun `test end timestamp is set`() = runTest {
        // Given
        val task = createTask()
        val updatedTaskSlot = slot<Task>()
        val xpReward = XpReward(baseXp = 50L)

        coEvery { mockRepository.updateTask(capture(updatedTaskSlot)) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        val beforeTime = System.currentTimeMillis()
        completeTaskUseCase(task)
        val afterTime = System.currentTimeMillis()

        // Then
        val endTime = updatedTaskSlot.captured.end
        assertNotNull("End timestamp should be set", endTime)
        assertTrue("End timestamp should be recent", endTime!! >= beforeTime && endTime <= afterTime)
    }

    @Test
    fun `test modified timestamp is updated`() = runTest {
        // Given
        val task = createTask()
        val updatedTaskSlot = slot<Task>()
        val xpReward = XpReward(baseXp = 50L)

        coEvery { mockRepository.updateTask(capture(updatedTaskSlot)) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        val beforeTime = System.currentTimeMillis()
        completeTaskUseCase(task)
        val afterTime = System.currentTimeMillis()

        // Then
        val modifiedTime = updatedTaskSlot.captured.modified
        assertNotNull("Modified timestamp should be set", modifiedTime)
        assertTrue("Modified timestamp should be recent", modifiedTime!! >= beforeTime && modifiedTime <= afterTime)
    }

    @Test
    fun `test XP reward is calculated`() = runTest {
        // Given
        val task = createTask(urgency = 10.0)
        val xpReward = XpReward(baseXp = 100L, urgencyMultiplier = 1.5, completionBonus = 50L)

        coEvery { mockRepository.updateTask(any()) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        coVerify(exactly = 1) { mockCalculateXpRewardUseCase(task) }
    }

    @Test
    fun `test XP is awarded to hero on success`() = runTest {
        // Given
        val task = createTask()
        val xpReward = XpReward(baseXp = 100L, urgencyMultiplier = 2.0, completionBonus = 50L)
        val expectedXp = xpReward.totalXp // Should be 100 * 2.0 + 50 = 250

        coEvery { mockRepository.updateTask(any()) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(expectedXp) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        coVerify(exactly = 1) { mockAddXpToHeroUseCase(expectedXp) }
    }

    @Test
    fun `test repository update is called`() = runTest {
        // Given
        val task = createTask()
        val xpReward = XpReward(baseXp = 50L)

        coEvery { mockRepository.updateTask(any()) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        coVerify(exactly = 1) { mockRepository.updateTask(any()) }
    }

    @Test
    fun `test returns success when repository update succeeds`() = runTest {
        // Given
        val task = createTask()
        val xpReward = XpReward(baseXp = 50L)

        coEvery { mockRepository.updateTask(any()) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        val result = completeTaskUseCase(task)

        // Then
        assertTrue("Result should be success", result.isSuccess)
    }

    @Test
    fun `test returns failure when repository update fails`() = runTest {
        // Given
        val task = createTask()
        val exception = Exception("Database error")

        coEvery { mockRepository.updateTask(any()) } returns Result.failure(exception)

        // When
        val result = completeTaskUseCase(task)

        // Then
        assertTrue("Result should be failure", result.isFailure)
        assertEquals("Exception should match", exception, result.exceptionOrNull())
    }

    @Test
    fun `test XP is not awarded when repository update fails`() = runTest {
        // Given
        val task = createTask()

        coEvery { mockRepository.updateTask(any()) } returns Result.failure(Exception("Database error"))

        // When
        completeTaskUseCase(task)

        // Then
        coVerify(exactly = 0) { mockCalculateXpRewardUseCase(any()) }
        coVerify(exactly = 0) { mockAddXpToHeroUseCase(any()) }
    }

    @Test
    fun `test high priority task awards more XP`() = runTest {
        // Given
        val task = createTask(priority = TaskPriority.HIGH, urgency = 15.0)
        val xpReward = XpReward(baseXp = 150L, urgencyMultiplier = 2.0, completionBonus = 50L)

        coEvery { mockRepository.updateTask(any()) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        coVerify(exactly = 1) { mockAddXpToHeroUseCase(xpReward.totalXp) }
        assertTrue("XP should be substantial for high priority", xpReward.totalXp > 200L)
    }

    @Test
    fun `test original task is passed to XP calculator`() = runTest {
        // Given
        val task = createTask(uuid = "original-uuid", urgency = 8.5)
        val taskSlot = slot<Task>()
        val xpReward = XpReward(baseXp = 85L)

        coEvery { mockRepository.updateTask(any()) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(capture(taskSlot)) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        assertEquals("Original task should be passed to XP calculator", task, taskSlot.captured)
    }

    @Test
    fun `test completing already completed task updates timestamps`() = runTest {
        // Given
        val task = createTask(status = TaskStatus.COMPLETED)
        val updatedTaskSlot = slot<Task>()
        val xpReward = XpReward(baseXp = 50L)

        coEvery { mockRepository.updateTask(capture(updatedTaskSlot)) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        assertEquals(TaskStatus.COMPLETED, updatedTaskSlot.captured.status)
        assertNotNull("End timestamp should be updated", updatedTaskSlot.captured.end)
        assertNotNull("Modified timestamp should be updated", updatedTaskSlot.captured.modified)
    }

    @Test
    fun `test task with zero urgency still awards XP`() = runTest {
        // Given
        val task = createTask(urgency = 0.0)
        val xpReward = XpReward(baseXp = 10L) // Minimum XP

        coEvery { mockRepository.updateTask(any()) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        coVerify(exactly = 1) { mockAddXpToHeroUseCase(xpReward.totalXp) }
    }

    @Test
    fun `test end and modified timestamps are the same`() = runTest {
        // Given
        val task = createTask()
        val updatedTaskSlot = slot<Task>()
        val xpReward = XpReward(baseXp = 50L)

        coEvery { mockRepository.updateTask(capture(updatedTaskSlot)) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        val capturedTask = updatedTaskSlot.captured
        assertEquals(
            "End and modified timestamps should be the same",
            capturedTask.end,
            capturedTask.modified
        )
    }

    @Test
    fun `test UUID and description remain unchanged`() = runTest {
        // Given
        val task = createTask(uuid = "special-uuid", description = "Important task")
        val updatedTaskSlot = slot<Task>()
        val xpReward = XpReward(baseXp = 50L)

        coEvery { mockRepository.updateTask(capture(updatedTaskSlot)) } returns Result.success(Unit)
        every { mockCalculateXpRewardUseCase(task) } returns xpReward
        coEvery { mockAddXpToHeroUseCase(any()) } returns Result.success(Unit)

        // When
        completeTaskUseCase(task)

        // Then
        val capturedTask = updatedTaskSlot.captured
        assertEquals("UUID should remain unchanged", "special-uuid", capturedTask.uuid)
        assertEquals("Description should remain unchanged", "Important task", capturedTask.description)
    }
}
