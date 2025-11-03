package com.taskhero.core.parser

import com.taskhero.domain.task.model.TaskFilter
import com.taskhero.domain.task.model.TaskPriority
import com.taskhero.domain.task.model.TaskStatus

/**
 * Builds SQL queries from TaskFilter objects for use with Room database.
 *
 * This class generates parameterized SQL WHERE clauses and provides the binding
 * arguments needed for safe query execution.
 */
class FilterQueryBuilder {

    /**
     * Result containing SQL query string and bind arguments.
     */
    data class QueryResult(
        val sql: String,
        val bindArgs: List<Any>
    )

    /**
     * Build a complete SQL query from a TaskFilter.
     *
     * @param filter The filter to convert to SQL
     * @param baseQuery The base SELECT query (defaults to selecting all columns from tasks table)
     * @return QueryResult containing the complete SQL and bind arguments
     */
    fun buildQuery(
        filter: TaskFilter,
        baseQuery: String = "SELECT * FROM tasks"
    ): QueryResult {
        val whereClause = buildWhereClause(filter)
        val sql = if (whereClause.sql.isNotEmpty()) {
            "$baseQuery WHERE ${whereClause.sql}"
        } else {
            baseQuery
        }

        return QueryResult(sql, whereClause.bindArgs)
    }

    /**
     * Build just the WHERE clause portion of the query.
     *
     * @param filter The filter to convert to WHERE clause
     * @return QueryResult containing the WHERE clause and bind arguments
     */
    fun buildWhereClause(filter: TaskFilter): QueryResult {
        val conditions = mutableListOf<String>()
        val bindArgs = mutableListOf<Any>()

        // UUID filter
        filter.uuid?.let {
            conditions.add("uuid = ?")
            bindArgs.add(it)
        }

        // Description filter (partial match)
        filter.description?.let {
            conditions.add("description LIKE ?")
            bindArgs.add("%$it%")
        }

        // Status filter (single status)
        filter.status?.let {
            conditions.add("status = ?")
            bindArgs.add(it.name)
        }

        // Status filter (multiple statuses)
        if (filter.statuses.isNotEmpty()) {
            val placeholders = filter.statuses.joinToString(",") { "?" }
            conditions.add("status IN ($placeholders)")
            filter.statuses.forEach { bindArgs.add(it.name) }
        }

        // Entry date range
        filter.entryStarting?.let {
            conditions.add("entry >= ?")
            bindArgs.add(it)
        }

        filter.entryUntil?.let {
            conditions.add("entry <= ?")
            bindArgs.add(it)
        }

        // Modified date
        filter.modified?.let {
            conditions.add("modified >= ?")
            bindArgs.add(it)
        }

        // Start date
        filter.start?.let {
            conditions.add("start >= ?")
            bindArgs.add(it)
        }

        // End date
        filter.end?.let {
            conditions.add("end <= ?")
            bindArgs.add(it)
        }

        // Due date
        filter.due?.let {
            conditions.add("due <= ?")
            bindArgs.add(it)
        }

        // Wait date
        filter.wait?.let {
            conditions.add("wait <= ?")
            bindArgs.add(it)
        }

        // Scheduled date
        filter.scheduled?.let {
            conditions.add("scheduled <= ?")
            bindArgs.add(it)
        }

        // Until date
        filter.until?.let {
            conditions.add("until <= ?")
            bindArgs.add(it)
        }

        // Project filter
        filter.project?.let {
            if (it.isEmpty()) {
                // Empty string means tasks with no project
                conditions.add("(project IS NULL OR project = '')")
            } else {
                conditions.add("project = ?")
                bindArgs.add(it)
            }
        }

        // Priority filter
        filter.priority?.let {
            conditions.add("priority = ?")
            bindArgs.add(it.name)
        }

        // Recurrence filter
        filter.recur?.let {
            conditions.add("recur = ?")
            bindArgs.add(it)
        }

        // Parent filter
        filter.parent?.let {
            conditions.add("parent = ?")
            bindArgs.add(it)
        }

        // Urgency range
        filter.urgencyMin?.let {
            conditions.add("urgency >= ?")
            bindArgs.add(it)
        }

        filter.urgencyMax?.let {
            conditions.add("urgency <= ?")
            bindArgs.add(it)
        }

        // Tags filter
        if (filter.tags.isNotEmpty()) {
            // For tags, we need to join with the task_tags table
            // This is a simplified version - adjust based on your schema
            val tagConditions = filter.tags.map {
                bindArgs.add(it)
                "uuid IN (SELECT task_uuid FROM task_tags WHERE tag = ?)"
            }
            conditions.add("(${tagConditions.joinToString(" AND ")})")
        }

        // Dependencies filter
        if (filter.dependencies.isNotEmpty()) {
            // Similar to tags, adjust based on your schema
            val depConditions = filter.dependencies.map {
                bindArgs.add(it)
                "uuid IN (SELECT task_uuid FROM task_dependencies WHERE dependency_uuid = ?)"
            }
            conditions.add("(${depConditions.joinToString(" AND ")})")
        }

        // Combine all conditions with AND logic
        val sql = conditions.joinToString(" AND ")

        return QueryResult(sql, bindArgs)
    }

    /**
     * Build a query with custom OR logic for multiple filter options.
     *
     * @param filters List of filters to combine with OR logic
     * @param baseQuery The base SELECT query
     * @return QueryResult containing the complete SQL and bind arguments
     */
    fun buildOrQuery(
        filters: List<TaskFilter>,
        baseQuery: String = "SELECT * FROM tasks"
    ): QueryResult {
        if (filters.isEmpty()) {
            return QueryResult(baseQuery, emptyList())
        }

        if (filters.size == 1) {
            return buildQuery(filters[0], baseQuery)
        }

        val orClauses = mutableListOf<String>()
        val allBindArgs = mutableListOf<Any>()

        filters.forEach { filter ->
            val whereClause = buildWhereClause(filter)
            if (whereClause.sql.isNotEmpty()) {
                orClauses.add("(${whereClause.sql})")
                allBindArgs.addAll(whereClause.bindArgs)
            }
        }

        val sql = if (orClauses.isNotEmpty()) {
            "$baseQuery WHERE ${orClauses.joinToString(" OR ")}"
        } else {
            baseQuery
        }

        return QueryResult(sql, allBindArgs)
    }

    /**
     * Build a count query from a filter.
     *
     * @param filter The filter to apply
     * @return QueryResult containing the count SQL and bind arguments
     */
    fun buildCountQuery(filter: TaskFilter): QueryResult {
        val whereClause = buildWhereClause(filter)
        val sql = if (whereClause.sql.isNotEmpty()) {
            "SELECT COUNT(*) FROM tasks WHERE ${whereClause.sql}"
        } else {
            "SELECT COUNT(*) FROM tasks"
        }

        return QueryResult(sql, whereClause.bindArgs)
    }

    /**
     * Add ORDER BY clause to a query.
     *
     * @param queryResult The existing query result
     * @param orderBy The column to order by
     * @param ascending Whether to sort ascending (default) or descending
     * @return Updated QueryResult with ORDER BY clause
     */
    fun addOrderBy(
        queryResult: QueryResult,
        orderBy: String,
        ascending: Boolean = true
    ): QueryResult {
        val direction = if (ascending) "ASC" else "DESC"
        val sql = "${queryResult.sql} ORDER BY $orderBy $direction"
        return QueryResult(sql, queryResult.bindArgs)
    }

    /**
     * Add LIMIT and OFFSET to a query.
     *
     * @param queryResult The existing query result
     * @param limit Maximum number of results
     * @param offset Number of results to skip
     * @return Updated QueryResult with LIMIT and OFFSET
     */
    fun addPagination(
        queryResult: QueryResult,
        limit: Int,
        offset: Int = 0
    ): QueryResult {
        val sql = "${queryResult.sql} LIMIT $limit OFFSET $offset"
        return QueryResult(sql, queryResult.bindArgs)
    }
}
