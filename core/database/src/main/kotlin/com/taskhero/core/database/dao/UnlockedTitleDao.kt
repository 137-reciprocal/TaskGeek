package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskhero.core.database.entity.UnlockedTitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockedTitleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unlockedTitle: UnlockedTitleEntity)

    @Update
    suspend fun update(unlockedTitle: UnlockedTitleEntity)

    @Delete
    suspend fun delete(unlockedTitle: UnlockedTitleEntity)

    @Query("SELECT * FROM unlocked_titles WHERE titleId = :titleId")
    fun getUnlockedTitle(titleId: String): Flow<UnlockedTitleEntity?>

    @Query("SELECT * FROM unlocked_titles")
    fun getAllUnlockedTitles(): Flow<List<UnlockedTitleEntity>>

    @Query("DELETE FROM unlocked_titles WHERE titleId = :titleId")
    suspend fun deleteByTitleId(titleId: String)

    @Query("SELECT COUNT(*) FROM unlocked_titles")
    fun getUnlockedTitleCount(): Flow<Int>

    @Query("SELECT * FROM unlocked_titles")
    suspend fun getAllUnlockedTitlesForBackup(): List<UnlockedTitleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnlockedTitle(unlockedTitle: UnlockedTitleEntity)
}
