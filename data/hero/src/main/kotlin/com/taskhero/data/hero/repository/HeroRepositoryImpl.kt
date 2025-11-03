package com.taskhero.data.hero.repository

import com.taskhero.core.database.dao.HeroDao
import com.taskhero.data.hero.mapper.HeroMapper.toDomain
import com.taskhero.data.hero.mapper.HeroMapper.toEntity
import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.repository.HeroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of HeroRepository that manages hero data using Room database.
 */
@Singleton
class HeroRepositoryImpl @Inject constructor(
    private val heroDao: HeroDao
) : HeroRepository {

    override fun getHero(): Flow<Hero?> {
        return heroDao.getHero().map { heroEntity ->
            heroEntity?.toDomain()
        }
    }

    override suspend fun updateHero(hero: Hero): Result<Unit> {
        return try {
            heroDao.update(hero.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createHero(hero: Hero): Result<Unit> {
        return try {
            heroDao.insert(hero.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
