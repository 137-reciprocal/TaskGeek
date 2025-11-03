package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskhero.core.database.entity.TimeEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for time entries.
 * Provides queries for time tracking operations.
 */
@Dao
interface TimeEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeEntry: TimeEntryEntity): Long

    @Update
    suspend fun update(timeEntry: TimeEntryEntity)

    @Delete
    suspend fun delete(timeEntry: TimeEntryEntity)

    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getById(id: Long): TimeEntryEntity?

    @Query("SELECT * FROM time_entries WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TimeEntryEntity?>

    @Query("SELECT * FROM time_entries WHERE end_time IS NULL LIMIT 1")
    fun getActiveEntry(): Flow<TimeEntryEntity?>

    @Query("SELECT * FROM time_entries WHERE task_uuid = :taskUuid ORDER BY start_time DESC")
    fun getEntriesForTask(taskUuid: String): Flow<List<TimeEntryEntity>>

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN end_time IS NULL THEN :currentTime - start_time
                ELSE end_time - start_time
            END
        ), 0)
        FROM time_entries
        WHERE task_uuid = :taskUuid
    """)
    fun getTotalTimeForTask(taskUuid: String, currentTime: Long = System.currentTimeMillis()): Flow<Long>

    @Query("""
        SELECT t.project, COALESCE(SUM(
            CASE
                WHEN te.end_time IS NULL THEN :currentTime - te.start_time
                ELSE te.end_time - te.start_time
            END
        ), 0) as total_time
        FROM time_entries te
        INNER JOIN tasks t ON te.task_uuid = t.uuid
        WHERE t.project IS NOT NULL
        GROUP BY t.project
    """)
    fun getTimeByProject(currentTime: Long = System.currentTimeMillis()): Flow<Map<String, Long>>

    @Query("""
        SELECT
            (start_time / 86400000) * 86400000 as day_start,
            COALESCE(SUM(
                CASE
                    WHEN end_time IS NULL THEN :currentTime - start_time
                    ELSE end_time - start_time
                END
            ), 0) as total_time
        FROM time_entries
        WHERE start_time >= :startDate AND start_time < :endDate
        GROUP BY day_start
        ORDER BY day_start
    """)
    fun getTimeByDay(
        startDate: Long,
        endDate: Long,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<Map<Long, Long>>

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN end_time IS NULL THEN :currentTime - start_time
                ELSE end_time - start_time
            END
        ), 0)
        FROM time_entries
        WHERE start_time >= :startDate AND start_time < :endDate
    """)
    fun getTotalTimeInRange(
        startDate: Long,
        endDate: Long,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<Long>

    @Query("DELETE FROM time_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM time_entries ORDER BY start_time DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 50): Flow<List<TimeEntryEntity>>
}
