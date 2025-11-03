package com.taskhero.feature.hero

/**
 * Sealed interface representing side effects for the Hero profile screen.
 */
sealed interface HeroEffect {
    /**
     * Show a snackbar with a message
     */
    data class ShowSnackbar(val message: String) : HeroEffect

    /**
     * Show avatar picker dialog
     */
    data object ShowAvatarPicker : HeroEffect
}
