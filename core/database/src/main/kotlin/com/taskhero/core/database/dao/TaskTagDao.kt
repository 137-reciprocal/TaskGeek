package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.taskhero.core.database.entity.TaskTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTagToTask(taskTag: TaskTagCrossRef)

    @Delete
    suspend fun removeTagFromTask(taskTag: TaskTagCrossRef)

    @Query("DELETE FROM task_tags WHERE taskUuid = :taskUuid AND tagName = :tagName")
    suspend fun removeTagFromTaskByName(taskUuid: String, tagName: String)

    @Query("SELECT tagName FROM task_tags WHERE taskUuid = :taskUuid")
    fun getTagsForTask(taskUuid: String): Flow<List<String>>

    @Query("SELECT taskUuid FROM task_tags WHERE tagName = :tagName")
    fun getTasksForTag(tagName: String): Flow<List<String>>

    @Query("SELECT * FROM task_tags WHERE taskUuid = :taskUuid")
    fun getTaskTagCrossRefsForTask(taskUuid: String): Flow<List<TaskTagCrossRef>>

    @Query("SELECT * FROM task_tags WHERE tagName = :tagName")
    fun getTaskTagCrossRefsForTag(tagName: String): Flow<List<TaskTagCrossRef>>

    @Query("DELETE FROM task_tags WHERE taskUuid = :taskUuid")
    suspend fun deleteAllTagsForTask(taskUuid: String)

    @Query("DELETE FROM task_tags WHERE tagName = :tagName")
    suspend fun deleteAllTasksForTag(tagName: String)

    @Query("SELECT * FROM task_tags")
    suspend fun getAllTaskTags(): List<TaskTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTag(taskTag: TaskTagCrossRef)
}
