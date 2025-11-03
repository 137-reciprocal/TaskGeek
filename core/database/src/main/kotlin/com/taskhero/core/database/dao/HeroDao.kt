package com.taskhero.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.taskhero.core.database.entity.HeroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hero: HeroEntity)

    @Update
    suspend fun update(hero: HeroEntity)

    @Delete
    suspend fun delete(hero: HeroEntity)

    @Query("SELECT * FROM hero WHERE id = 1")
    fun getHero(): Flow<HeroEntity?>

    @Query("DELETE FROM hero WHERE id = 1")
    suspend fun deleteHero()

    @Query("SELECT * FROM hero")
    suspend fun getAllHeroes(): List<HeroEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHero(hero: HeroEntity)
}
