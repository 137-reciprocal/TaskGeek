package com.taskhero.feature.hero.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Composable for displaying a single D&D-style stat.
 *
 * @param statName The name of the stat (e.g., "STR", "DEX")
 * @param statValue The stat value (typically 1-20)
 * @param modifier Modifier for the card
 */
@Composable
fun StatCard(
    statName: String,
    statValue: Int,
    modifier: Modifier = Modifier
) {
    // Calculate D&D modifier: (stat - 10) / 2 (rounded down)
    val modifier = (statValue - 10) / 2
    val modifierText = if (modifier >= 0) "+$modifier" else "$modifier"

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Stat name
            Text(
                text = statName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Stat value (large)
            Text(
                text = statValue.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // D&D modifier
            Text(
                text = modifierText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
