package com.taskhero.feature.taskdetail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tag chip component with a remove button.
 *
 * @param tag The tag text to display
 * @param onRemove Callback when the remove button is clicked
 * @param modifier Modifier for the chip
 */
@Composable
fun TagChip(
    tag: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { /* Tag chips are not clickable */ },
        label = { Text(tag) },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove tag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier
    )
}
