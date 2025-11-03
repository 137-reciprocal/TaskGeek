package com.taskhero.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 Shape System
val Shapes = Shapes(
    // Extra small components (chips)
    extraSmall = RoundedCornerShape(4.dp),

    // Small components (buttons)
    small = RoundedCornerShape(8.dp),

    // Medium components (cards)
    medium = RoundedCornerShape(12.dp),

    // Large components (bottom sheets, dialogs)
    large = RoundedCornerShape(16.dp),

    // Extra large components (large modals)
    extraLarge = RoundedCornerShape(28.dp)
)
