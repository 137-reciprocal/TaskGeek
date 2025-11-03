package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.taskhero.core.database.entity.TaskDependencyCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDependencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDependency(dependency: TaskDependencyCrossRef)

    @Delete
    suspend fun removeDependency(dependency: TaskDependencyCrossRef)

    @Query("DELETE FROM task_dependencies WHERE taskUuid = :taskUuid AND dependsOnUuid = :dependsOnUuid")
    suspend fun removeDependencyByUuid(taskUuid: String, dependsOnUuid: String)

    @Query("SELECT dependsOnUuid FROM task_dependencies WHERE taskUuid = :taskUuid")
    fun getDependenciesForTask(taskUuid: String): Flow<List<String>>

    @Query("SELECT taskUuid FROM task_dependencies WHERE dependsOnUuid = :taskUuid")
    fun getDependentsOfTask(taskUuid: String): Flow<List<String>>

    @Query("SELECT * FROM task_dependencies WHERE taskUuid = :taskUuid")
    fun getDependencyCrossRefsForTask(taskUuid: String): Flow<List<TaskDependencyCrossRef>>

    @Query("SELECT * FROM task_dependencies WHERE dependsOnUuid = :taskUuid")
    fun getDependentCrossRefsForTask(taskUuid: String): Flow<List<TaskDependencyCrossRef>>

    @Query("DELETE FROM task_dependencies WHERE taskUuid = :taskUuid")
    suspend fun deleteAllDependenciesForTask(taskUuid: String)

    @Query("DELETE FROM task_dependencies WHERE dependsOnUuid = :taskUuid")
    suspend fun deleteAllDependentsForTask(taskUuid: String)

    @Query("SELECT COUNT(*) FROM task_dependencies WHERE taskUuid = :taskUuid")
    fun getDependencyCount(taskUuid: String): Flow<Int>

    @Query("SELECT * FROM task_dependencies")
    suspend fun getAllDependencies(): List<TaskDependencyCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDependency(dependency: TaskDependencyCrossRef)
}
