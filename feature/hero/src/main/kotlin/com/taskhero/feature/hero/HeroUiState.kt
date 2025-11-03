package com.taskhero.feature.hero

import com.taskhero.domain.hero.model.Hero
import com.taskhero.domain.hero.model.Title
import com.taskhero.domain.hero.model.XpHistoryItem

/**
 * Sealed interface representing the UI state for the Hero profile screen.
 */
sealed interface HeroUiState {
    /**
     * Loading state - data is being fetched
     */
    data object Loading : HeroUiState

    /**
     * Success state - data has been loaded successfully
     */
    data class Success(
        val hero: Hero,
        val titles: List<Title>,
        val recentXpHistory: List<XpHistoryItem>
    ) : HeroUiState

    /**
     * Error state - an error occurred while loading data
     */
    data class Error(val message: String) : HeroUiState
}
