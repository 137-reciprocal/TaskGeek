package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskhero.core.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity)

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags WHERE name = :name")
    fun getTagByName(name: String): Flow<TagEntity?>

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("DELETE FROM tags WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("SELECT COUNT(*) FROM tags")
    fun getTagCount(): Flow<Int>

    @Query("SELECT * FROM tags")
    suspend fun getAllTagsForBackup(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)
}
