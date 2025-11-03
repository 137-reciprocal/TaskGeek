package com.taskhero.core.parser

import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for FilterQueryBuilder.
 *
 * Tests:
 * - Simple filters (status, project, priority)
 * - Complex filters (multiple conditions)
 * - Date range filters
 * - Tag filters
 * - SQL injection prevention
 * - Query building with AND logic
 * - Query building with OR logic
 */
class FilterQueryBuilderTest {

    private lateinit var queryBuilder: FilterQueryBuilder
    private val currentTime = System.currentTimeMillis()

    @Before
    fun setup() {
        queryBuilder = FilterQueryBuilder()
    }

    @Test
    fun `test simple status filter`() {
        // Given
        val filter = TaskFilter(status = TaskStatus.PENDING)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should contain WHERE clause", result.sql.contains("WHERE"))
        assertTrue("Query should filter by status", result.sql.contains("status = ?"))
        assertEquals("Should have 1 bind argument", 1, result.bindArgs.size)
        assertEquals("Bind argument should be PENDING", "PENDING", result.bindArgs[0])
    }

    @Test
    fun `test simple project filter`() {
        // Given
        val filter = TaskFilter(project = "work")

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by project", result.sql.contains("project = ?"))
        assertEquals("Should have 1 bind argument", 1, result.bindArgs.size)
        assertEquals("Bind argument should be work", "work", result.bindArgs[0])
    }

    @Test
    fun `test simple priority filter`() {
        // Given
        val filter = TaskFilter(priority = TaskPriority.HIGH)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by priority", result.sql.contains("priority = ?"))
        assertEquals("Should have 1 bind argument", 1, result.bindArgs.size)
        assertEquals("Bind argument should be HIGH", "HIGH", result.bindArgs[0])
    }

    @Test
    fun `test description filter uses LIKE`() {
        // Given
        val filter = TaskFilter(description = "meeting")

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should use LIKE for description", result.sql.contains("description LIKE ?"))
        assertEquals("Should have 1 bind argument", 1, result.bindArgs.size)
        assertEquals("Bind argument should have wildcards", "%meeting%", result.bindArgs[0])
    }

    @Test
    fun `test UUID filter`() {
        // Given
        val filter = TaskFilter(uuid = "test-uuid-123")

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by uuid", result.sql.contains("uuid = ?"))
        assertEquals("Bind argument should be test-uuid-123", "test-uuid-123", result.bindArgs[0])
    }

    @Test
    fun `test multiple statuses filter`() {
        // Given
        val filter = TaskFilter(statuses = listOf(TaskStatus.PENDING, TaskStatus.WAITING))

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should use IN clause", result.sql.contains("status IN (?,?)"))
        assertEquals("Should have 2 bind arguments", 2, result.bindArgs.size)
        assertTrue("Should contain PENDING", result.bindArgs.contains("PENDING"))
        assertTrue("Should contain WAITING", result.bindArgs.contains("WAITING"))
    }

    @Test
    fun `test date range filter for entry`() {
        // Given
        val startTime = currentTime - 1000000L
        val endTime = currentTime
        val filter = TaskFilter(entryStarting = startTime, entryUntil = endTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should have entry >= ?", result.sql.contains("entry >= ?"))
        assertTrue("Query should have entry <= ?", result.sql.contains("entry <= ?"))
        assertEquals("Should have 2 bind arguments", 2, result.bindArgs.size)
        assertEquals("First arg should be start time", startTime, result.bindArgs[0])
        assertEquals("Second arg should be end time", endTime, result.bindArgs[1])
    }

    @Test
    fun `test due date filter`() {
        // Given
        val dueTime = currentTime + 86400000L // 1 day from now
        val filter = TaskFilter(due = dueTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by due", result.sql.contains("due <= ?"))
        assertEquals("Should have 1 bind argument", 1, result.bindArgs.size)
        assertEquals("Bind argument should be due time", dueTime, result.bindArgs[0])
    }

    @Test
    fun `test modified date filter`() {
        // Given
        val modifiedTime = currentTime
        val filter = TaskFilter(modified = modifiedTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by modified", result.sql.contains("modified >= ?"))
        assertEquals("Bind argument should be modified time", modifiedTime, result.bindArgs[0])
    }

    @Test
    fun `test tags filter`() {
        // Given
        val filter = TaskFilter(tags = listOf("urgent", "important"))

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should contain task_tags subquery", result.sql.contains("task_tags"))
        assertEquals("Should have 2 bind arguments for tags", 2, result.bindArgs.size)
        assertTrue("Should contain urgent tag", result.bindArgs.contains("urgent"))
        assertTrue("Should contain important tag", result.bindArgs.contains("important"))
    }

    @Test
    fun `test dependencies filter`() {
        // Given
        val filter = TaskFilter(dependencies = listOf("dep-1", "dep-2"))

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should contain task_dependencies subquery", result.sql.contains("task_dependencies"))
        assertEquals("Should have 2 bind arguments for dependencies", 2, result.bindArgs.size)
    }

    @Test
    fun `test urgency range filter`() {
        // Given
        val filter = TaskFilter(urgencyMin = 5.0, urgencyMax = 15.0)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should have urgency >= ?", result.sql.contains("urgency >= ?"))
        assertTrue("Query should have urgency <= ?", result.sql.contains("urgency <= ?"))
        assertEquals("Should have 2 bind arguments", 2, result.bindArgs.size)
        assertEquals("First arg should be min urgency", 5.0, result.bindArgs[0])
        assertEquals("Second arg should be max urgency", 15.0, result.bindArgs[1])
    }

    @Test
    fun `test complex filter with multiple conditions`() {
        // Given
        val filter = TaskFilter(
            status = TaskStatus.PENDING,
            priority = TaskPriority.HIGH,
            project = "work"
        )

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should contain status condition", result.sql.contains("status = ?"))
        assertTrue("Query should contain priority condition", result.sql.contains("priority = ?"))
        assertTrue("Query should contain project condition", result.sql.contains("project = ?"))
        assertTrue("Conditions should be combined with AND", result.sql.contains("AND"))
        assertEquals("Should have 3 bind arguments", 3, result.bindArgs.size)
    }

    @Test
    fun `test empty filter returns base query`() {
        // Given
        val filter = TaskFilter()

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertEquals("Query should be base query only", "SELECT * FROM tasks", result.sql)
        assertTrue("Should have no bind arguments", result.bindArgs.isEmpty())
    }

    @Test
    fun `test custom base query`() {
        // Given
        val filter = TaskFilter(status = TaskStatus.PENDING)
        val baseQuery = "SELECT uuid, description FROM tasks"

        // When
        val result = queryBuilder.buildQuery(filter, baseQuery)

        // Then
        assertTrue("Query should start with custom base", result.sql.startsWith(baseQuery))
        assertTrue("Query should have WHERE clause", result.sql.contains("WHERE"))
    }

    @Test
    fun `test buildWhereClause without base query`() {
        // Given
        val filter = TaskFilter(status = TaskStatus.PENDING, priority = TaskPriority.HIGH)

        // When
        val result = queryBuilder.buildWhereClause(filter)

        // Then
        assertTrue("Should contain status condition", result.sql.contains("status = ?"))
        assertTrue("Should contain priority condition", result.sql.contains("priority = ?"))
        assertTrue("Should not contain SELECT", !result.sql.contains("SELECT"))
    }

    @Test
    fun `test buildOrQuery with multiple filters`() {
        // Given
        val filters = listOf(
            TaskFilter(status = TaskStatus.PENDING),
            TaskFilter(status = TaskStatus.WAITING)
        )

        // When
        val result = queryBuilder.buildOrQuery(filters)

        // Then
        assertTrue("Query should contain OR", result.sql.contains("OR"))
        assertEquals("Should have 2 bind arguments", 2, result.bindArgs.size)
    }

    @Test
    fun `test buildOrQuery with single filter`() {
        // Given
        val filters = listOf(TaskFilter(status = TaskStatus.PENDING))

        // When
        val result = queryBuilder.buildOrQuery(filters)

        // Then
        assertTrue("Query should contain WHERE", result.sql.contains("WHERE"))
        assertTrue("Query should not contain OR", !result.sql.contains("OR"))
    }

    @Test
    fun `test buildOrQuery with empty list`() {
        // Given
        val filters = emptyList<TaskFilter>()

        // When
        val result = queryBuilder.buildOrQuery(filters)

        // Then
        assertEquals("Should return base query", "SELECT * FROM tasks", result.sql)
        assertTrue("Should have no bind arguments", result.bindArgs.isEmpty())
    }

    @Test
    fun `test buildCountQuery`() {
        // Given
        val filter = TaskFilter(status = TaskStatus.PENDING)

        // When
        val result = queryBuilder.buildCountQuery(filter)

        // Then
        assertTrue("Query should be COUNT query", result.sql.contains("SELECT COUNT(*)"))
        assertTrue("Query should have WHERE clause", result.sql.contains("WHERE"))
        assertTrue("Query should filter by status", result.sql.contains("status = ?"))
    }

    @Test
    fun `test addOrderBy ascending`() {
        // Given
        val initialQuery = FilterQueryBuilder.QueryResult("SELECT * FROM tasks", emptyList())

        // When
        val result = queryBuilder.addOrderBy(initialQuery, "urgency", ascending = true)

        // Then
        assertTrue("Query should have ORDER BY", result.sql.contains("ORDER BY urgency ASC"))
    }

    @Test
    fun `test addOrderBy descending`() {
        // Given
        val initialQuery = FilterQueryBuilder.QueryResult("SELECT * FROM tasks", emptyList())

        // When
        val result = queryBuilder.addOrderBy(initialQuery, "due", ascending = false)

        // Then
        assertTrue("Query should have ORDER BY DESC", result.sql.contains("ORDER BY due DESC"))
    }

    @Test
    fun `test addPagination`() {
        // Given
        val initialQuery = FilterQueryBuilder.QueryResult("SELECT * FROM tasks", emptyList())

        // When
        val result = queryBuilder.addPagination(initialQuery, limit = 20, offset = 10)

        // Then
        assertTrue("Query should have LIMIT", result.sql.contains("LIMIT 20"))
        assertTrue("Query should have OFFSET", result.sql.contains("OFFSET 10"))
    }

    @Test
    fun `test addPagination without offset`() {
        // Given
        val initialQuery = FilterQueryBuilder.QueryResult("SELECT * FROM tasks", emptyList())

        // When
        val result = queryBuilder.addPagination(initialQuery, limit = 50)

        // Then
        assertTrue("Query should have LIMIT", result.sql.contains("LIMIT 50"))
        assertTrue("Query should have OFFSET 0", result.sql.contains("OFFSET 0"))
    }

    @Test
    fun `test empty project filter queries for null or empty`() {
        // Given
        val filter = TaskFilter(project = "")

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should check for NULL", result.sql.contains("project IS NULL"))
        assertTrue("Query should check for empty", result.sql.contains("project = ''"))
        assertTrue("Should use OR for NULL or empty", result.sql.contains("OR"))
    }

    @Test
    fun `test SQL injection prevention in description`() {
        // Given
        val maliciousInput = "'; DROP TABLE tasks; --"
        val filter = TaskFilter(description = maliciousInput)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Should use parameterized query", result.sql.contains("description LIKE ?"))
        assertEquals("Malicious input should be in bind args", "%$maliciousInput%", result.bindArgs[0])
        assertTrue("Query should not contain DROP", !result.sql.contains("DROP"))
    }

    @Test
    fun `test SQL injection prevention in project`() {
        // Given
        val maliciousInput = "work' OR '1'='1"
        val filter = TaskFilter(project = maliciousInput)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Should use parameterized query", result.sql.contains("project = ?"))
        assertEquals("Input should be safely bound", maliciousInput, result.bindArgs[0])
    }

    @Test
    fun `test recurrence filter`() {
        // Given
        val filter = TaskFilter(recur = "weekly")

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by recur", result.sql.contains("recur = ?"))
        assertEquals("Bind argument should be weekly", "weekly", result.bindArgs[0])
    }

    @Test
    fun `test parent filter`() {
        // Given
        val filter = TaskFilter(parent = "parent-uuid")

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by parent", result.sql.contains("parent = ?"))
        assertEquals("Bind argument should be parent-uuid", "parent-uuid", result.bindArgs[0])
    }

    @Test
    fun `test wait filter`() {
        // Given
        val waitTime = currentTime + 100000L
        val filter = TaskFilter(wait = waitTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by wait", result.sql.contains("wait <= ?"))
        assertEquals("Bind argument should be wait time", waitTime, result.bindArgs[0])
    }

    @Test
    fun `test scheduled filter`() {
        // Given
        val scheduledTime = currentTime
        val filter = TaskFilter(scheduled = scheduledTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by scheduled", result.sql.contains("scheduled <= ?"))
        assertEquals("Bind argument should be scheduled time", scheduledTime, result.bindArgs[0])
    }

    @Test
    fun `test until filter`() {
        // Given
        val untilTime = currentTime + 200000L
        val filter = TaskFilter(until = untilTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by until", result.sql.contains("until <= ?"))
        assertEquals("Bind argument should be until time", untilTime, result.bindArgs[0])
    }

    @Test
    fun `test combining filters, order by, and pagination`() {
        // Given
        val filter = TaskFilter(status = TaskStatus.PENDING)

        // When
        var result = queryBuilder.buildQuery(filter)
        result = queryBuilder.addOrderBy(result, "urgency", ascending = false)
        result = queryBuilder.addPagination(result, limit = 10, offset = 0)

        // Then
        assertTrue("Query should have WHERE", result.sql.contains("WHERE"))
        assertTrue("Query should have ORDER BY", result.sql.contains("ORDER BY urgency DESC"))
        assertTrue("Query should have LIMIT", result.sql.contains("LIMIT 10"))
    }

    @Test
    fun `test end date filter`() {
        // Given
        val endTime = currentTime
        val filter = TaskFilter(end = endTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by end", result.sql.contains("end <= ?"))
        assertEquals("Bind argument should be end time", endTime, result.bindArgs[0])
    }

    @Test
    fun `test start date filter`() {
        // Given
        val startTime = currentTime
        val filter = TaskFilter(start = startTime)

        // When
        val result = queryBuilder.buildQuery(filter)

        // Then
        assertTrue("Query should filter by start", result.sql.contains("start >= ?"))
        assertEquals("Bind argument should be start time", startTime, result.bindArgs[0])
    }
}
