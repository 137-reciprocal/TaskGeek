package com.taskhero.data.timetracking.repository

import com.taskhero.core.database.dao.TimeEntryDao
import com.taskhero.core.database.entity.TimeEntryEntity
import com.taskhero.domain.timetracking.model.TimeEntry
import com.taskhero.domain.timetracking.repository.TimeTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of TimeTrackingRepository using Room database.
 */
class TimeTrackingRepositoryImpl @Inject constructor(
    private val timeEntryDao: TimeEntryDao
) : TimeTrackingRepository {

    override suspend fun startTracking(taskUuid: String): Result<TimeEntry> {
        return try {
            val entity = TimeEntryEntity(
                taskUuid = taskUuid,
                startTime = System.currentTimeMillis(),
                endTime = null
            )
            val id = timeEntryDao.insert(entity)
            val createdEntry = entity.copy(id = id).toDomain()
            Result.success(createdEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopTracking(entryId: Long): Result<TimeEntry> {
        return try {
            val entity = timeEntryDao.getById(entryId)
                ?: return Result.failure(IllegalArgumentException("Time entry not found"))

            if (entity.endTime != null) {
                return Result.failure(IllegalStateException("Time entry already stopped"))
            }

            val updatedEntity = entity.copy(endTime = System.currentTimeMillis())
            timeEntryDao.update(updatedEntity)
            Result.success(updatedEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveEntry(): Flow<TimeEntry?> {
        return timeEntryDao.getActiveEntry().map { it?.toDomain() }
    }

    override fun getEntriesForTask(taskUuid: String): Flow<List<TimeEntry>> {
        return timeEntryDao.getEntriesForTask(taskUuid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalTimeForTask(taskUuid: String): Flow<Long> {
        return timeEntryDao.getTotalTimeForTask(taskUuid)
    }

    override fun getTimeByProject(): Flow<Map<String, Long>> {
        return timeEntryDao.getTimeByProject()
    }

    override fun getTimeByDay(startDate: Long, endDate: Long): Flow<Map<Long, Long>> {
        return timeEntryDao.getTimeByDay(startDate, endDate)
    }

    override fun getTotalTimeInRange(startDate: Long, endDate: Long): Flow<Long> {
        return timeEntryDao.getTotalTimeInRange(startDate, endDate)
    }

    override suspend fun deleteEntry(entryId: Long): Result<Unit> {
        return try {
            timeEntryDao.deleteById(entryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Converts TimeEntryEntity to domain TimeEntry model.
     */
    private fun TimeEntryEntity.toDomain(): TimeEntry {
        return TimeEntry(
            id = id,
            taskUuid = taskUuid,
            startTime = startTime,
            endTime = endTime,
            tags = tags.split(",").filter { it.isNotBlank() }
        )
    }
}
