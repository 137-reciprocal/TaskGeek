package com.taskhero.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dimensions object containing standardized spacing, padding, and elevation values
 * for consistent UI across the application.
 */
object Dimensions {

    // Spacing values
    object Spacing {
        val none = 0.dp
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val extraLarge = 32.dp
        val huge = 48.dp
    }

    // Padding values
    object Padding {
        val none = 0.dp
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val extraLarge = 32.dp
    }

    // Elevation values
    object Elevation {
        val none = 0.dp
        val small = 2.dp
        val medium = 4.dp
        val large = 8.dp
        val extraLarge = 16.dp
    }

    // Corner radius values
    object CornerRadius {
        val none = 0.dp
        val small = 4.dp
        val medium = 8.dp
        val large = 12.dp
        val extraLarge = 16.dp
        val round = 28.dp
    }

    // Icon sizes
    object IconSize {
        val small = 16.dp
        val medium = 24.dp
        val large = 32.dp
        val extraLarge = 48.dp
    }

    // Common component sizes
    object ComponentSize {
        val buttonHeight = 48.dp
        val inputHeight = 56.dp
        val toolbarHeight = 56.dp
        val bottomNavHeight = 80.dp
        val fabSize = 56.dp
    }
}
