package com.taskhero.feature.taskdetail.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable

/**
 * Material 3 Date Picker Dialog for selecting task due dates.
 *
 * @param currentDate The currently selected date in milliseconds (null if no date selected)
 * @param onDismiss Callback when dialog is dismissed
 * @param onDateSelected Callback when a date is selected, provides timestamp in milliseconds
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerDialog(
    currentDate: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis)
                    }
                    onDismiss()
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true
        )
    }
}
