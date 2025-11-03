package com.taskhero.domain.timetracking.repository

import com.taskhero.domain.timetracking.model.TimeEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for time tracking operations.
 * Provides methods to track time spent on tasks.
 */
interface TimeTrackingRepository {
    /**
     * Starts tracking time for a task.
     *
     * @param taskUuid UUID of the task to track
     * @return Result containing the created TimeEntry or an error
     */
    suspend fun startTracking(taskUuid: String): Result<TimeEntry>

    /**
     * Stops tracking time for a time entry.
     *
     * @param entryId ID of the time entry to stop
     * @return Result containing the updated TimeEntry or an error
     */
    suspend fun stopTracking(entryId: Long): Result<TimeEntry>

    /**
     * Gets the currently active time entry (if any).
     *
     * @return Flow emitting the active TimeEntry or null
     */
    fun getActiveEntry(): Flow<TimeEntry?>

    /**
     * Gets all time entries for a specific task.
     *
     * @param taskUuid UUID of the task
     * @return Flow emitting list of TimeEntries
     */
    fun getEntriesForTask(taskUuid: String): Flow<List<TimeEntry>>

    /**
     * Gets the total time spent on a task in milliseconds.
     *
     * @param taskUuid UUID of the task
     * @return Flow emitting the total duration in milliseconds
     */
    fun getTotalTimeForTask(taskUuid: String): Flow<Long>

    /**
     * Gets all time entries grouped by project.
     *
     * @return Flow emitting a map of project names to total time in milliseconds
     */
    fun getTimeByProject(): Flow<Map<String, Long>>

    /**
     * Gets all time entries grouped by day.
     *
     * @param startDate Start date in milliseconds since epoch
     * @param endDate End date in milliseconds since epoch
     * @return Flow emitting a map of dates to total time in milliseconds
     */
    fun getTimeByDay(startDate: Long, endDate: Long): Flow<Map<Long, Long>>

    /**
     * Gets the total time tracked in a date range.
     *
     * @param startDate Start date in milliseconds since epoch
     * @param endDate End date in milliseconds since epoch
     * @return Flow emitting the total duration in milliseconds
     */
    fun getTotalTimeInRange(startDate: Long, endDate: Long): Flow<Long>

    /**
     * Deletes a time entry.
     *
     * @param entryId ID of the time entry to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteEntry(entryId: Long): Result<Unit>
}
