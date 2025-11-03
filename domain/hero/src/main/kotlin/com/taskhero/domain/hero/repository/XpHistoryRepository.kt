package com.taskhero.domain.hero.repository

import com.taskhero.domain.hero.model.XpHistoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for XP history operations.
 */
interface XpHistoryRepository {
    /**
     * Get all XP history items ordered by timestamp descending.
     */
    fun getAllXpHistory(): Flow<List<XpHistoryItem>>

    /**
     * Get recent XP history items (limited).
     */
    fun getRecentXpHistory(limit: Int): Flow<List<XpHistoryItem>>

    /**
     * Get XP history for a specific task.
     */
    fun getXpHistoryForTask(taskUuid: String): Flow<List<XpHistoryItem>>

    /**
     * Insert a new XP history entry.
     */
    suspend fun insertXpHistory(xpHistory: XpHistoryItem): Result<Unit>
}
