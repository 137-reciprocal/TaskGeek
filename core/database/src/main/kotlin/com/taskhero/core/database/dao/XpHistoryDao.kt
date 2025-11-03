package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskhero.core.database.entity.XpHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface XpHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(xpHistory: XpHistoryEntity)

    @Update
    suspend fun update(xpHistory: XpHistoryEntity)

    @Delete
    suspend fun delete(xpHistory: XpHistoryEntity)

    @Query("SELECT * FROM xp_history WHERE id = :id")
    fun getXpHistoryById(id: Long): Flow<XpHistoryEntity?>

    @Query("SELECT * FROM xp_history WHERE taskUuid = :taskUuid ORDER BY timestamp DESC")
    fun getXpHistoryForTask(taskUuid: String): Flow<List<XpHistoryEntity>>

    @Query("SELECT * FROM xp_history WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getXpHistoryForDateRange(startTime: Long, endTime: Long): Flow<List<XpHistoryEntity>>

    @Query("SELECT * FROM xp_history ORDER BY timestamp DESC")
    fun getAllXpHistory(): Flow<List<XpHistoryEntity>>

    @Query("DELETE FROM xp_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(xpEarned) FROM xp_history WHERE taskUuid = :taskUuid")
    fun getTotalXpForTask(taskUuid: String): Flow<Long?>

    @Query("SELECT SUM(xpEarned) FROM xp_history WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalXpForDateRange(startTime: Long, endTime: Long): Flow<Long?>

    @Query("SELECT * FROM xp_history")
    suspend fun getAllXpHistoryForBackup(): List<XpHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXpHistory(xpHistory: XpHistoryEntity)
}
