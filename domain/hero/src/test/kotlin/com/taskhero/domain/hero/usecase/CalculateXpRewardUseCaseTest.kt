package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.XpReward
import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for CalculateXpRewardUseCase.
 *
 * Tests:
 * - Base XP calculation from urgency
 * - Priority multipliers (High=2.0, Medium=1.5, Low=1.0)
 * - Due date bonuses (+50 for early completion)
 * - Due date penalties (0 XP penalty for late completion, but no bonus)
 * - Edge cases (zero urgency, no priority, no due date)
 */
class CalculateXpRewardUseCaseTest {

    private lateinit var calculateXpRewardUseCase: CalculateXpRewardUseCase
    private val currentTime = System.currentTimeMillis()
    private val dayMs = 24 * 60 * 60 * 1000L

    @Before
    fun setup() {
        calculateXpRewardUseCase = CalculateXpRewardUseCase()
    }

    private fun createTask(
        urgency: Double = 5.0,
        priority: TaskPriority? = null,
        due: Long? = null
    ): Task {
        return Task(
            uuid = "test-uuid",
            description = "Test task",
            status = TaskStatus.PENDING,
            entry = currentTime,
            urgency = urgency,
            priority = priority,
            due = due
        )
    }

    @Test
    fun `test base XP calculation from urgency`() {
        // Given
        val task = createTask(urgency = 10.0)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Base XP should be urgency * 10", 100L, reward.baseXp)
    }

    @Test
    fun `test base XP with zero urgency`() {
        // Given
        val task = createTask(urgency = 0.0)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Base XP should be 0", 0L, reward.baseXp)
    }

    @Test
    fun `test base XP with high urgency`() {
        // Given
        val task = createTask(urgency = 25.0)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Base XP should be 250", 250L, reward.baseXp)
    }

    @Test
    fun `test priority high multiplier`() {
        // Given
        val task = createTask(urgency = 10.0, priority = TaskPriority.HIGH)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // High priority multiplier = 2.0
        // With urgency >= 10.0, urgency multiplier = 1.3
        // Combined: 2.0 * 1.3 = 2.6
        assertEquals("Urgency multiplier should include priority", 2.6, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test priority medium multiplier`() {
        // Given
        val task = createTask(urgency = 10.0, priority = TaskPriority.MEDIUM)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // Medium priority multiplier = 1.5
        // With urgency >= 10.0, urgency multiplier = 1.3
        // Combined: 1.5 * 1.3 = 1.95
        assertEquals("Urgency multiplier should include priority", 1.95, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test priority low multiplier`() {
        // Given
        val task = createTask(urgency = 10.0, priority = TaskPriority.LOW)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // Low priority multiplier = 1.0
        // With urgency >= 10.0, urgency multiplier = 1.3
        // Combined: 1.0 * 1.3 = 1.3
        assertEquals("Urgency multiplier should include priority", 1.3, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test no priority defaults to 1_0 multiplier`() {
        // Given
        val task = createTask(urgency = 10.0, priority = null)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // No priority multiplier = 1.0
        // With urgency >= 10.0, urgency multiplier = 1.3
        // Combined: 1.0 * 1.3 = 1.3
        assertEquals("Urgency multiplier without priority", 1.3, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test due date early completion bonus`() {
        // Given
        val futureTime = currentTime + (2 * dayMs) // 2 days in future
        val task = createTask(due = futureTime)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Should get early completion bonus", 50L, reward.completionBonus)
    }

    @Test
    fun `test due date on time completion bonus`() {
        // Given
        val task = createTask(due = currentTime) // Due now

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Should get bonus for on-time completion", 50L, reward.completionBonus)
    }

    @Test
    fun `test due date late completion has no bonus`() {
        // Given
        val pastTime = currentTime - (1 * dayMs) // 1 day in past
        val task = createTask(due = pastTime)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Should have no bonus for late completion", 0L, reward.completionBonus)
    }

    @Test
    fun `test no due date means no completion bonus`() {
        // Given
        val task = createTask(due = null)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Should have no bonus without due date", 0L, reward.completionBonus)
    }

    @Test
    fun `test urgency multiplier for very urgent tasks`() {
        // Given
        val task = createTask(urgency = 20.0) // >= 15.0

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Very urgent multiplier should be 1.5", 1.5, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test urgency multiplier for urgent tasks`() {
        // Given
        val task = createTask(urgency = 12.0) // >= 10.0, < 15.0

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Urgent multiplier should be 1.3", 1.3, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test urgency multiplier for moderate urgency tasks`() {
        // Given
        val task = createTask(urgency = 7.0) // >= 5.0, < 10.0

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Moderate urgency multiplier should be 1.1", 1.1, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test urgency multiplier for low urgency tasks`() {
        // Given
        val task = createTask(urgency = 3.0) // < 5.0

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Low urgency multiplier should be 1.0", 1.0, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test total XP calculation`() {
        // Given
        val task = createTask(urgency = 10.0, priority = TaskPriority.HIGH, due = currentTime + dayMs)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // Base XP = 10.0 * 10 = 100
        // Urgency multiplier = 1.3 (urgency >= 10) * 2.0 (high priority) = 2.6
        // Completion bonus = 50
        // Total = (100 * 2.6) + 50 = 260 + 50 = 310
        assertEquals("Total XP should be calculated correctly", 310L, reward.totalXp)
    }

    @Test
    fun `test total XP with no bonuses`() {
        // Given
        val task = createTask(urgency = 5.0, priority = null, due = null)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // Base XP = 5.0 * 10 = 50
        // Urgency multiplier = 1.1 (urgency >= 5.0, < 10.0)
        // Completion bonus = 0
        // Total = (50 * 1.1) + 0 = 55
        assertEquals("Total XP should be base * urgency multiplier", 55L, reward.totalXp)
    }

    @Test
    fun `test hasBonus returns true with completion bonus`() {
        // Given
        val task = createTask(due = currentTime + dayMs)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertTrue("Should have bonus with early completion", reward.hasBonus())
    }

    @Test
    fun `test hasBonus returns true with urgency multiplier greater than 1`() {
        // Given
        val task = createTask(urgency = 10.0) // Urgency multiplier = 1.3

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertTrue("Should have bonus with urgency multiplier > 1.0", reward.hasBonus())
    }

    @Test
    fun `test hasBonus returns false with no bonuses`() {
        // Given
        val task = createTask(urgency = 3.0, due = null) // Low urgency, no due date

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertTrue("Should not have bonus with multiplier 1.0 and no completion bonus", !reward.hasBonus())
    }

    @Test
    fun `test high priority very urgent task gets maximum multiplier`() {
        // Given
        val task = createTask(urgency = 20.0, priority = TaskPriority.HIGH)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // Urgency multiplier = 1.5 (very urgent) * 2.0 (high priority) = 3.0
        assertEquals("Should have maximum multiplier", 3.0, reward.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test medium priority moderate urgency with early completion`() {
        // Given
        val task = createTask(
            urgency = 7.0,
            priority = TaskPriority.MEDIUM,
            due = currentTime + (3 * dayMs)
        )

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        // Base XP = 70
        // Multiplier = 1.1 * 1.5 = 1.65
        // Completion bonus = 50
        // Total = (70 * 1.65) + 50 = 115.5 + 50 = 165
        assertEquals("Base XP should be 70", 70L, reward.baseXp)
        assertEquals("Multiplier should be 1.65", 1.65, reward.urgencyMultiplier, 0.01)
        assertEquals("Completion bonus should be 50", 50L, reward.completionBonus)
        assertEquals("Total XP should be 165", 165L, reward.totalXp)
    }

    @Test
    fun `test getFormattedXp returns string`() {
        // Given
        val task = createTask(urgency = 10.0)

        // When
        val reward = calculateXpRewardUseCase(task)
        val formatted = reward.getFormattedXp()

        // Then
        assertTrue("Formatted XP should be numeric string", formatted.toLongOrNull() != null)
        assertEquals("Formatted XP should match totalXp", reward.totalXp.toString(), formatted)
    }

    @Test
    fun `test urgency boundary at 15_0`() {
        // Given
        val task1 = createTask(urgency = 14.99)
        val task2 = createTask(urgency = 15.0)

        // When
        val reward1 = calculateXpRewardUseCase(task1)
        val reward2 = calculateXpRewardUseCase(task2)

        // Then
        assertEquals("14.99 should have 1.3 multiplier", 1.3, reward1.urgencyMultiplier, 0.01)
        assertEquals("15.0 should have 1.5 multiplier", 1.5, reward2.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test urgency boundary at 10_0`() {
        // Given
        val task1 = createTask(urgency = 9.99)
        val task2 = createTask(urgency = 10.0)

        // When
        val reward1 = calculateXpRewardUseCase(task1)
        val reward2 = calculateXpRewardUseCase(task2)

        // Then
        assertEquals("9.99 should have 1.1 multiplier", 1.1, reward1.urgencyMultiplier, 0.01)
        assertEquals("10.0 should have 1.3 multiplier", 1.3, reward2.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test urgency boundary at 5_0`() {
        // Given
        val task1 = createTask(urgency = 4.99)
        val task2 = createTask(urgency = 5.0)

        // When
        val reward1 = calculateXpRewardUseCase(task1)
        val reward2 = calculateXpRewardUseCase(task2)

        // Then
        assertEquals("4.99 should have 1.0 multiplier", 1.0, reward1.urgencyMultiplier, 0.01)
        assertEquals("5.0 should have 1.1 multiplier", 1.1, reward2.urgencyMultiplier, 0.01)
    }

    @Test
    fun `test extremely high urgency task`() {
        // Given
        val task = createTask(urgency = 100.0, priority = TaskPriority.HIGH, due = currentTime + dayMs)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Base XP should be 1000", 1000L, reward.baseXp)
        assertEquals("Should have very urgent multiplier with high priority", 3.0, reward.urgencyMultiplier, 0.01)
        assertEquals("Should have completion bonus", 50L, reward.completionBonus)
        // Total = (1000 * 3.0) + 50 = 3050
        assertEquals("Total XP should be 3050", 3050L, reward.totalXp)
    }

    @Test
    fun `test fractional urgency rounds correctly`() {
        // Given
        val task = createTask(urgency = 5.7)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Base XP should be 57 (5.7 * 10)", 57L, reward.baseXp)
    }

    @Test
    fun `test late completion by many days still has no bonus`() {
        // Given
        val veryPastTime = currentTime - (30 * dayMs) // 30 days overdue
        val task = createTask(due = veryPastTime)

        // When
        val reward = calculateXpRewardUseCase(task)

        // Then
        assertEquals("Should have no bonus for very late completion", 0L, reward.completionBonus)
    }

    @Test
    fun `test all priority levels with same urgency`() {
        // Given
        val baseUrgency = 10.0
        val taskHigh = createTask(urgency = baseUrgency, priority = TaskPriority.HIGH)
        val taskMedium = createTask(urgency = baseUrgency, priority = TaskPriority.MEDIUM)
        val taskLow = createTask(urgency = baseUrgency, priority = TaskPriority.LOW)
        val taskNone = createTask(urgency = baseUrgency, priority = null)

        // When
        val rewardHigh = calculateXpRewardUseCase(taskHigh)
        val rewardMedium = calculateXpRewardUseCase(taskMedium)
        val rewardLow = calculateXpRewardUseCase(taskLow)
        val rewardNone = calculateXpRewardUseCase(taskNone)

        // Then
        assertTrue("High priority should earn more than medium", rewardHigh.totalXp > rewardMedium.totalXp)
        assertTrue("Medium priority should earn more than low", rewardMedium.totalXp > rewardLow.totalXp)
        assertEquals("Low and no priority should earn same", rewardLow.totalXp, rewardNone.totalXp)
    }
}
