package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskhero.core.database.entity.TaskEntity
import com.taskhero.domain.task.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE uuid = :uuid")
    fun getTaskByUuid(uuid: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY urgency DESC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING' AND (wait IS NULL OR wait <= :now) ORDER BY urgency DESC LIMIT 50")
    fun getNextTasks(now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks WHERE uuid = :uuid")
    suspend fun deleteByUuid(uuid: String)

    @Query("SELECT * FROM tasks WHERE project LIKE :projectPrefix || '%'")
    fun getTasksByProject(projectPrefix: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksForBackup(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
}
