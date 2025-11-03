package com.taskhero.feature.taskdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tag selector dialog with autocomplete functionality.
 * Allows selecting from available tags or creating new ones.
 *
 * @param availableTags List of all available tags in the system
 * @param selectedTags Currently selected tags for this task
 * @param onDismiss Callback when dialog is dismissed
 * @param onTagsSelected Callback when tags are confirmed, provides list of selected tags
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelectorDialog(
    availableTags: List<String>,
    selectedTags: List<String>,
    onDismiss: () -> Unit,
    onTagsSelected: (List<String>) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedTags) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTags = remember(availableTags, searchQuery, currentSelection) {
        availableTags
            .filter { tag ->
                tag.contains(searchQuery, ignoreCase = true) &&
                !currentSelection.contains(tag)
            }
            .take(10) // Limit suggestions
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Tags") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Currently selected tags
                if (currentSelection.isNotEmpty()) {
                    Text("Selected:", modifier = Modifier.padding(bottom = 4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        currentSelection.forEach { tag ->
                            FilterChip(
                                selected = true,
                                onClick = {
                                    currentSelection = currentSelection - tag
                                },
                                label = { Text(tag) }
                            )
                        }
                    }
                }

                // Search/Create field with autocomplete
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search or create tag") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (searchQuery.isNotBlank() && !currentSelection.contains(searchQuery)) {
                            IconButton(
                                onClick = {
                                    currentSelection = currentSelection + searchQuery.trim()
                                    searchQuery = ""
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add tag")
                            }
                        }
                    },
                    placeholder = { Text("Type to search or create...") }
                )

                // Suggestions
                if (filteredTags.isNotEmpty()) {
                    Text("Suggestions:", modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredTags) { tag ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    currentSelection = currentSelection + tag
                                    searchQuery = ""
                                },
                                label = { Text(tag) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTagsSelected(currentSelection)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
