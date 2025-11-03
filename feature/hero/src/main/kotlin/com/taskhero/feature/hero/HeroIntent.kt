package com.taskhero.feature.hero

/**
 * Sealed interface representing user intents for the Hero profile screen.
 */
sealed interface HeroIntent {
    /**
     * Load hero data
     */
    data object LoadHero : HeroIntent

    /**
     * Update the hero's display name
     */
    data class UpdateDisplayName(val name: String) : HeroIntent

    /**
     * Update the hero's avatar
     */
    data class UpdateAvatar(val uri: String) : HeroIntent

    /**
     * Update the hero's class type
     */
    data class UpdateClass(val classType: String) : HeroIntent

    /**
     * Select a title for the hero
     */
    data class SelectTitle(val titleId: String) : HeroIntent
}
