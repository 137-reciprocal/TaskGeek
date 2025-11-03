package com.taskhero.feature.taskdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UDA (User Defined Attribute) data types supported by TaskWarrior.
 */
enum class UdaType {
    STRING,
    NUMBER,
    DATE,
    DURATION;

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * UDA data class for editing.
 */
data class UdaEntry(
    val key: String,
    val value: Any?,
    val type: UdaType
)

/**
 * UDA Editor composable for managing User Defined Attributes.
 * Allows adding, editing, and deleting UDAs with type-specific value inputs.
 *
 * @param udas Current map of UDAs (key -> value)
 * @param onAddUda Callback for adding a new UDA
 * @param onUpdateUda Callback for updating an existing UDA
 * @param onDeleteUda Callback for deleting a UDA
 * @param modifier Modifier for the composable
 */
@Composable
fun UdaEditor(
    udas: Map<String, Any?>,
    onAddUda: (String, Any?, UdaType) -> Unit,
    onUpdateUda: (String, Any?, UdaType) -> Unit,
    onDeleteUda: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingUda by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "User Defined Attributes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add UDA"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add UDA")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display existing UDAs
        if (udas.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "No User Defined Attributes. Click 'Add UDA' to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    udas.forEach { (key, value) ->
                        UdaItem(
                            key = key,
                            value = value,
                            onEdit = { editingUda = key },
                            onDelete = { onDeleteUda(key) }
                        )
                    }
                }
            }
        }
    }

    // Add UDA Dialog
    if (showAddDialog) {
        UdaEditDialog(
            title = "Add UDA",
            initialKey = "",
            initialValue = null,
            initialType = UdaType.STRING,
            onDismiss = { showAddDialog = false },
            onConfirm = { key, value, type ->
                onAddUda(key, value, type)
                showAddDialog = false
            }
        )
    }

    // Edit UDA Dialog
    editingUda?.let { key ->
        val currentValue = udas[key]
        val currentType = inferUdaType(currentValue)

        UdaEditDialog(
            title = "Edit UDA",
            initialKey = key,
            initialValue = currentValue,
            initialType = currentType,
            keyReadOnly = true,
            onDismiss = { editingUda = null },
            onConfirm = { _, value, type ->
                onUpdateUda(key, value, type)
                editingUda = null
            }
        )
    }
}

/**
 * Individual UDA item display.
 */
@Composable
private fun UdaItem(
    key: String,
    value: Any?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = key,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatUdaValue(value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $key",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete $key",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Dialog for adding or editing a UDA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UdaEditDialog(
    title: String,
    initialKey: String,
    initialValue: Any?,
    initialType: UdaType,
    keyReadOnly: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Any?, UdaType) -> Unit,
    modifier: Modifier = Modifier
) {
    var key by remember { mutableStateOf(initialKey) }
    var valueString by remember { mutableStateOf(formatUdaValue(initialValue)) }
    var selectedType by remember { mutableStateOf(initialType) }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Key field
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = keyReadOnly,
                    enabled = !keyReadOnly,
                    placeholder = { Text("e.g., estimate, customer") }
                )

                // Type selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        UdaType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.toString()) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Value field with type-specific input
                OutlinedTextField(
                    value = valueString,
                    onValueChange = {
                        valueString = it
                        errorMessage = null
                    },
                    label = { Text("Value") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(getPlaceholderForType(selectedType)) },
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (key.isBlank()) {
                        errorMessage = "Key cannot be empty"
                        return@Button
                    }

                    val parsedValue = parseUdaValue(valueString, selectedType)
                    if (parsedValue == null && valueString.isNotBlank()) {
                        errorMessage = "Invalid value for type ${selectedType.toString().lowercase()}"
                        return@Button
                    }

                    onConfirm(key, parsedValue, selectedType)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

/**
 * Infer the UDA type from its value.
 */
private fun inferUdaType(value: Any?): UdaType {
    return when (value) {
        is Number -> UdaType.NUMBER
        is Long -> UdaType.DATE // Assume Long values are timestamps
        else -> UdaType.STRING
    }
}

/**
 * Format UDA value for display.
 */
private fun formatUdaValue(value: Any?): String {
    return when (value) {
        null -> ""
        is Long -> {
            // Try to format as date if it's a reasonable timestamp
            if (value > 1000000000000L) { // Timestamp in milliseconds
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(value))
            } else if (value > 1000000000L) { // Timestamp in seconds
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(value * 1000))
            } else {
                value.toString()
            }
        }
        else -> value.toString()
    }
}

/**
 * Parse UDA value from string based on type.
 */
private fun parseUdaValue(valueString: String, type: UdaType): Any? {
    if (valueString.isBlank()) return null

    return try {
        when (type) {
            UdaType.STRING -> valueString
            UdaType.NUMBER -> {
                // Try to parse as Long first, then Double
                valueString.toLongOrNull() ?: valueString.toDouble()
            }
            UdaType.DATE -> {
                // Try to parse as ISO date or use current timestamp
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .parse(valueString)?.time
                    ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .parse(valueString)?.time
            }
            UdaType.DURATION -> {
                // Parse duration string (e.g., "2h", "30m", "1d")
                parseDuration(valueString)
            }
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Parse duration string into seconds.
 * Supports formats like: 1d, 2h, 30m, 1h30m, etc.
 */
private fun parseDuration(duration: String): Long? {
    val pattern = """(\d+)([dhms])""".toRegex()
    val matches = pattern.findAll(duration.lowercase())

    var totalSeconds = 0L
    matches.forEach { match ->
        val (amount, unit) = match.destructured
        val seconds = when (unit) {
            "d" -> amount.toLong() * 86400
            "h" -> amount.toLong() * 3600
            "m" -> amount.toLong() * 60
            "s" -> amount.toLong()
            else -> 0L
        }
        totalSeconds += seconds
    }

    return if (totalSeconds > 0) totalSeconds else null
}

/**
 * Get placeholder text for different UDA types.
 */
private fun getPlaceholderForType(type: UdaType): String {
    return when (type) {
        UdaType.STRING -> "Enter text value"
        UdaType.NUMBER -> "Enter number (e.g., 42 or 3.14)"
        UdaType.DATE -> "YYYY-MM-DD HH:mm"
        UdaType.DURATION -> "e.g., 2h, 30m, 1d"
    }
}
