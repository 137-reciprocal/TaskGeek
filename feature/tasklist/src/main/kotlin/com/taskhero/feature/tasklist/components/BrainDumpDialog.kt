package com.taskhero.feature.tasklist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.taskhero.core.parser.BrainDumpParser
import com.taskhero.core.parser.ParsedTaskData

/**
 * Full-screen brain dump dialog for quickly adding multiple tasks.
 *
 * Supports:
 * - Multi-line text input (8-10 lines visible)
 * - Comma-separated or newline-separated tasks
 * - Real-time parsing showing count
 * - Preview list of parsed tasks
 * - Keyboard shortcut (Ctrl/Cmd+Enter) to add all
 *
 * @param onDismiss Callback when dialog should be dismissed
 * @param onAddTasks Callback when tasks should be added with list of parsed tasks
 * @param modifier Modifier for the dialog
 */
@Composable
fun BrainDumpDialog(
    onDismiss: () -> Unit,
    onAddTasks: (List<ParsedTaskData>) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    var parsedTasks by remember { mutableStateOf<List<ParsedTaskData>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Parse input in real-time
    LaunchedEffect(input) {
        parsedTasks = BrainDumpParser.parse(input)
    }

    // Request focus when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Brain Dump",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${parsedTasks.size} task${if (parsedTasks.size != 1) "s" else ""} ready to add",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close brain dump"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input field
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .focusRequester(focusRequester)
                        .onKeyEvent { keyEvent ->
                            // Handle Ctrl/Cmd+Enter to add all tasks
                            if (keyEvent.type == KeyEventType.KeyDown &&
                                keyEvent.key == Key.Enter &&
                                (keyEvent.isCtrlPressed || keyEvent.isMetaPressed)
                            ) {
                                if (parsedTasks.isNotEmpty()) {
                                    onAddTasks(parsedTasks)
                                    onDismiss()
                                }
                                true
                            } else {
                                false
                            }
                        },
                    placeholder = {
                        Text("Empty your mind... (one task per line or comma-separated)")
                    },
                    supportingText = {
                        Text(
                            text = "Try: Buy milk tomorrow, Call dentist p1, Review code #work\nOr one task per line. Press Ctrl/Cmd+Enter to add all.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.None
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    minLines = 8,
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preview section
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preview list
                if (parsedTasks.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No tasks yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Start typing to see your tasks appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    BrainDumpPreview(
                        parsedTasks = parsedTasks,
                        onEditTask = { index, newDescription ->
                            // Update the task in the input
                            val tasks = BrainDumpParser.parse(input)
                            if (index in tasks.indices) {
                                // Replace the task at the given index
                                val updatedInput = input.lines().toMutableList()
                                if (index in updatedInput.indices) {
                                    updatedInput[index] = newDescription
                                    input = updatedInput.joinToString("\n")
                                }
                            }
                        },
                        onDeleteTask = { index ->
                            // Remove the task from the input
                            val lines = input.lines().toMutableList()
                            if (index in lines.indices) {
                                lines.removeAt(index)
                                input = lines.joinToString("\n")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (parsedTasks.isNotEmpty()) {
                                onAddTasks(parsedTasks)
                                onDismiss()
                            }
                        },
                        enabled = parsedTasks.isNotEmpty()
                    ) {
                        Text("Add All Tasks")
                    }
                }
            }
        }
    }
}
