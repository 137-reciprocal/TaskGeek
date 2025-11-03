package com.taskhero.data.hero.repository

import com.taskhero.core.database.dao.XpHistoryDao
import com.taskhero.data.hero.mapper.toDomain
import com.taskhero.data.hero.mapper.toEntity
import com.taskhero.domain.hero.model.XpHistoryItem
import com.taskhero.domain.hero.repository.XpHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of XpHistoryRepository
 */
class XpHistoryRepositoryImpl @Inject constructor(
    private val xpHistoryDao: XpHistoryDao
) : XpHistoryRepository {

    override fun getAllXpHistory(): Flow<List<XpHistoryItem>> {
        return xpHistoryDao.getAllXpHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentXpHistory(limit: Int): Flow<List<XpHistoryItem>> {
        return xpHistoryDao.getAllXpHistory().map { entities ->
            entities.take(limit).map { it.toDomain() }
        }
    }

    override fun getXpHistoryForTask(taskUuid: String): Flow<List<XpHistoryItem>> {
        return xpHistoryDao.getXpHistoryForTask(taskUuid).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertXpHistory(xpHistory: XpHistoryItem): Result<Unit> {
        return try {
            xpHistoryDao.insert(xpHistory.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
