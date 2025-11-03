package com.taskhero.domain.task.usecase

import com.taskhero.domain.task.model.Task
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for CalculateUrgencyUseCase.
 *
 * Tests all 14 coefficients:
 * 1. Priority High (6.0)
 * 2. Priority Medium (3.9)
 * 3. Priority Low (1.8)
 * 4. Tag "next" (15.0)
 * 5. Active status (4.0)
 * 6. Scheduled status (5.0)
 * 7. Due imminent < 24h (12.0)
 * 8. Due very soon < 3d (9.0)
 * 9. Due soon < 7d (6.0)
 * 10. Due near < 14d (3.0)
 * 11. Due far < 30d (1.5)
 * 12. Due distant > 30d (0.2)
 * 13. Blocking dependencies (8.0 per task)
 * 14. Blocked by dependencies (-5.0 per task)
 * Plus: Overdue (15.0), Age factor (0.1 per day, max 2.0)
 */
class CalculateUrgencyUseCaseTest {

    private lateinit var calculateUrgencyUseCase: CalculateUrgencyUseCase

    private val currentTime = System.currentTimeMillis()
    private val dayMs = 24 * 60 * 60 * 1000L

    @Before
    fun setup() {
        calculateUrgencyUseCase = CalculateUrgencyUseCase()
    }

    // Helper function to create a base task
    private fun createTask(
        priority: TaskPriority? = null,
        due: Long? = null,
        tags: List<String> = emptyList(),
        start: Long? = null,
        scheduled: Long? = null,
        entry: Long = currentTime
    ): Task {
        return Task(
            uuid = "test-uuid",
            description = "Test task",
            status = TaskStatus.PENDING,
            entry = entry,
            priority = priority,
            due = due,
            tags = tags,
            start = start,
            scheduled = scheduled,
            urgency = 0.0
        )
    }

    // Test 1: Priority High coefficient
    @Test
    fun `test priority high adds 6_0 to urgency`() {
        val task = createTask(priority = TaskPriority.HIGH)
        val urgency = calculateUrgencyUseCase(task)

        // Priority high = 6.0, no other factors
        assertTrue("Urgency should be >= 6.0", urgency >= 6.0)
        assertTrue("Urgency should be < 7.0 (only priority + small age bonus)", urgency < 7.0)
    }

    // Test 2: Priority Medium coefficient
    @Test
    fun `test priority medium adds 3_9 to urgency`() {
        val task = createTask(priority = TaskPriority.MEDIUM)
        val urgency = calculateUrgencyUseCase(task)

        // Priority medium = 3.9
        assertTrue("Urgency should be >= 3.9", urgency >= 3.9)
        assertTrue("Urgency should be < 5.0", urgency < 5.0)
    }

    // Test 3: Priority Low coefficient
    @Test
    fun `test priority low adds 1_8 to urgency`() {
        val task = createTask(priority = TaskPriority.LOW)
        val urgency = calculateUrgencyUseCase(task)

        // Priority low = 1.8
        assertTrue("Urgency should be >= 1.8", urgency >= 1.8)
        assertTrue("Urgency should be < 3.0", urgency < 3.0)
    }

    // Test 4: Tag "next" coefficient
    @Test
    fun `test next tag adds 15_0 to urgency`() {
        val task = createTask(tags = listOf("next"))
        val urgency = calculateUrgencyUseCase(task)

        // Tag "next" = 15.0
        assertTrue("Urgency should be >= 15.0", urgency >= 15.0)
        assertTrue("Urgency should be < 16.0", urgency < 16.0)
    }

    // Test 5: Active status coefficient
    @Test
    fun `test active status adds 4_0 to urgency`() {
        val task = createTask(start = currentTime)
        val urgency = calculateUrgencyUseCase(task)

        // Active (has start time) = 4.0
        assertTrue("Urgency should be >= 4.0", urgency >= 4.0)
        assertTrue("Urgency should be < 5.0", urgency < 5.0)
    }

    // Test 6: Scheduled status coefficient
    @Test
    fun `test scheduled status adds 5_0 to urgency`() {
        val task = createTask(scheduled = currentTime)
        val urgency = calculateUrgencyUseCase(task)

        // Scheduled = 5.0
        assertTrue("Urgency should be >= 5.0", urgency >= 5.0)
        assertTrue("Urgency should be < 6.0", urgency < 6.0)
    }

    // Test 7: Due imminent < 24h coefficient
    @Test
    fun `test due imminent less than 24h adds 12_0 to urgency`() {
        val dueTime = currentTime + (12 * 60 * 60 * 1000L) // 12 hours from now
        val task = createTask(due = dueTime)
        val urgency = calculateUrgencyUseCase(task)

        // Due imminent = 12.0
        assertTrue("Urgency should be >= 12.0", urgency >= 12.0)
        assertTrue("Urgency should be < 13.0", urgency < 13.0)
    }

    // Test 8: Due very soon < 3d coefficient
    @Test
    fun `test due very soon less than 3d adds 9_0 to urgency`() {
        val dueTime = currentTime + (2 * dayMs) // 2 days from now
        val task = createTask(due = dueTime)
        val urgency = calculateUrgencyUseCase(task)

        // Due very soon = 9.0
        assertTrue("Urgency should be >= 9.0", urgency >= 9.0)
        assertTrue("Urgency should be < 10.0", urgency < 10.0)
    }

    // Test 9: Due soon < 7d coefficient
    @Test
    fun `test due soon less than 7d adds 6_0 to urgency`() {
        val dueTime = currentTime + (5 * dayMs) // 5 days from now
        val task = createTask(due = dueTime)
        val urgency = calculateUrgencyUseCase(task)

        // Due soon = 6.0
        assertTrue("Urgency should be >= 6.0", urgency >= 6.0)
        assertTrue("Urgency should be < 7.0", urgency < 7.0)
    }

    // Test 10: Due near < 14d coefficient
    @Test
    fun `test due near less than 14d adds 3_0 to urgency`() {
        val dueTime = currentTime + (10 * dayMs) // 10 days from now
        val task = createTask(due = dueTime)
        val urgency = calculateUrgencyUseCase(task)

        // Due near = 3.0
        assertTrue("Urgency should be >= 3.0", urgency >= 3.0)
        assertTrue("Urgency should be < 4.0", urgency < 4.0)
    }

    // Test 11: Due far < 30d coefficient
    @Test
    fun `test due far less than 30d adds 1_5 to urgency`() {
        val dueTime = currentTime + (20 * dayMs) // 20 days from now
        val task = createTask(due = dueTime)
        val urgency = calculateUrgencyUseCase(task)

        // Due far = 1.5
        assertTrue("Urgency should be >= 1.5", urgency >= 1.5)
        assertTrue("Urgency should be < 2.5", urgency < 2.5)
    }

    // Test 12: Due distant > 30d coefficient
    @Test
    fun `test due distant more than 30d adds 0_2 to urgency`() {
        val dueTime = currentTime + (40 * dayMs) // 40 days from now
        val task = createTask(due = dueTime)
        val urgency = calculateUrgencyUseCase(task)

        // Due distant = 0.2
        assertTrue("Urgency should be >= 0.2", urgency >= 0.2)
        assertTrue("Urgency should be < 1.0", urgency < 1.0)
    }

    // Test 13: Blocking dependencies coefficient
    @Test
    fun `test blocking dependencies adds 8_0 per task`() {
        val task = createTask()
        val urgency = calculateUrgencyUseCase(task, blockingCount = 2)

        // Blocking = 8.0 * 2 = 16.0
        assertTrue("Urgency should be >= 16.0", urgency >= 16.0)
        assertTrue("Urgency should be < 17.0", urgency < 17.0)
    }

    // Test 14: Blocked by dependencies coefficient
    @Test
    fun `test blocked by dependencies subtracts 5_0 per task`() {
        val task = createTask(priority = TaskPriority.HIGH) // Start with some urgency
        val urgency = calculateUrgencyUseCase(task, blockedCount = 1)

        // Priority high = 6.0, blocked = -5.0, result = 1.0 + age bonus
        assertTrue("Urgency should be >= 1.0", urgency >= 1.0)
        assertTrue("Urgency should be < 3.0", urgency < 3.0)
    }

    // Edge case: Overdue task
    @Test
    fun `test overdue task adds 15_0 to urgency`() {
        val dueTime = currentTime - (1 * dayMs) // 1 day ago
        val task = createTask(due = dueTime)
        val urgency = calculateUrgencyUseCase(task)

        // Overdue = 15.0
        assertTrue("Urgency should be >= 15.0", urgency >= 15.0)
        assertTrue("Urgency should be < 16.0", urgency < 16.0)
    }

    // Edge case: No due date
    @Test
    fun `test task with no due date has no due date coefficient`() {
        val task = createTask(priority = TaskPriority.MEDIUM)
        val urgency = calculateUrgencyUseCase(task)

        // Only priority medium = 3.9, no due date coefficient
        assertTrue("Urgency should be >= 3.9", urgency >= 3.9)
        assertTrue("Urgency should be < 5.0", urgency < 5.0)
    }

    // Test: Age calculation
    @Test
    fun `test age factor increases urgency`() {
        val entryTime = currentTime - (10 * dayMs) // 10 days ago
        val task = createTask(entry = entryTime)
        val urgency = calculateUrgencyUseCase(task)

        // Age factor = 10 * 0.1 = 1.0
        assertTrue("Urgency should be >= 1.0 (age bonus)", urgency >= 1.0)
        assertTrue("Urgency should be < 2.0", urgency < 2.0)
    }

    // Test: Max age bonus cap
    @Test
    fun `test age factor is capped at 2_0`() {
        val entryTime = currentTime - (30 * dayMs) // 30 days ago
        val task = createTask(entry = entryTime)
        val urgency = calculateUrgencyUseCase(task)

        // Age factor capped at 2.0
        assertTrue("Urgency should be >= 2.0", urgency >= 2.0)
        assertTrue("Urgency should be <= 2.1", urgency <= 2.1)
    }

    // Test: Combined factors
    @Test
    fun `test combined factors accumulate correctly`() {
        val dueTime = currentTime + (2 * dayMs) // 2 days from now
        val task = createTask(
            priority = TaskPriority.HIGH,
            due = dueTime,
            tags = listOf("next"),
            start = currentTime,
            scheduled = currentTime
        )
        val urgency = calculateUrgencyUseCase(task)

        // Priority high = 6.0
        // Due very soon = 9.0
        // Tag next = 15.0
        // Active = 4.0
        // Scheduled = 5.0
        // Age bonus = ~0
        // Total = ~39.0
        assertTrue("Urgency should be >= 39.0", urgency >= 39.0)
        assertTrue("Urgency should be < 40.0", urgency < 40.0)
    }

    // Test: Tag matching is case insensitive
    @Test
    fun `test next tag is case insensitive`() {
        val taskLower = createTask(tags = listOf("next"))
        val taskUpper = createTask(tags = listOf("NEXT"))
        val taskMixed = createTask(tags = listOf("NeXt"))

        val urgencyLower = calculateUrgencyUseCase(taskLower)
        val urgencyUpper = calculateUrgencyUseCase(taskUpper)
        val urgencyMixed = calculateUrgencyUseCase(taskMixed)

        assertEquals("All next tag variations should have same urgency", urgencyLower, urgencyUpper, 0.01)
        assertEquals("All next tag variations should have same urgency", urgencyLower, urgencyMixed, 0.01)
    }

    // Test: Urgency is never negative
    @Test
    fun `test urgency is never negative even with many blocked dependencies`() {
        val task = createTask()
        val urgency = calculateUrgencyUseCase(task, blockedCount = 10)

        assertTrue("Urgency should never be negative", urgency >= 0.0)
    }

    // Test: Multiple blocking dependencies
    @Test
    fun `test multiple blocking dependencies multiply correctly`() {
        val task = createTask()
        val urgency = calculateUrgencyUseCase(task, blockingCount = 3)

        // 3 * 8.0 = 24.0, plus age bonus
        assertTrue("Urgency should be >= 24.0", urgency >= 24.0)
        assertTrue("Urgency should be < 25.0", urgency < 25.0)
    }

    // Test: Priority impact on urgency
    @Test
    fun `test high priority has greater urgency than medium and low`() {
        val taskHigh = createTask(priority = TaskPriority.HIGH)
        val taskMedium = createTask(priority = TaskPriority.MEDIUM)
        val taskLow = createTask(priority = TaskPriority.LOW)

        val urgencyHigh = calculateUrgencyUseCase(taskHigh)
        val urgencyMedium = calculateUrgencyUseCase(taskMedium)
        val urgencyLow = calculateUrgencyUseCase(taskLow)

        assertTrue("High priority should have greater urgency than medium", urgencyHigh > urgencyMedium)
        assertTrue("Medium priority should have greater urgency than low", urgencyMedium > urgencyLow)
    }

    // Test: No priority
    @Test
    fun `test task with no priority has minimal urgency`() {
        val task = createTask(priority = null)
        val urgency = calculateUrgencyUseCase(task)

        // Only age bonus
        assertTrue("Urgency should be small (only age bonus)", urgency < 1.0)
    }
}
