package com.taskhero.domain.hero.usecase

import com.taskhero.domain.hero.model.XpHistoryItem
import com.taskhero.domain.hero.repository.XpHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get recent XP history for display.
 */
class GetXpHistoryUseCase @Inject constructor(
    private val repository: XpHistoryRepository
) {
    /**
     * Get recent XP history items.
     * @param limit Maximum number of items to return (default: 10)
     */
    operator fun invoke(limit: Int = 10): Flow<List<XpHistoryItem>> {
        return repository.getRecentXpHistory(limit)
    }
}
