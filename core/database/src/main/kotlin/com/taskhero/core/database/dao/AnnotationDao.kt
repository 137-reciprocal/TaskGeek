package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskhero.core.database.entity.AnnotationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(annotation: AnnotationEntity)

    @Update
    suspend fun update(annotation: AnnotationEntity)

    @Delete
    suspend fun delete(annotation: AnnotationEntity)

    @Query("SELECT * FROM annotations WHERE id = :id")
    fun getAnnotationById(id: Long): Flow<AnnotationEntity?>

    @Query("SELECT * FROM annotations WHERE taskUuid = :taskUuid ORDER BY timestamp DESC")
    fun getAnnotationsForTask(taskUuid: String): Flow<List<AnnotationEntity>>

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM annotations")
    fun getAllAnnotations(): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations")
    suspend fun getAllAnnotationsForBackup(): List<AnnotationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity)
}
