package com.taskhero.feature.hero.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Image picker dialog that allows users to select an image from gallery.
 * Handles permission requests automatically.
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onImageSelected Callback when an image is selected, provides the URI
 */
@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    var showPermissionDenied by remember { mutableStateOf(false) }

    // Permission launcher for READ_EXTERNAL_STORAGE (for older Android versions)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showPermissionDenied = true
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
        onDismiss()
    }

    // Launch the image picker immediately when this composable is shown
    LaunchedEffect(Unit) {
        imagePickerLauncher.launch("image/*")
    }

    // Show permission denied dialog if needed
    if (showPermissionDenied) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDenied = false
                onDismiss()
            },
            title = { Text("Permission Required") },
            text = {
                Text("Storage permission is required to select an avatar image. Please grant permission in app settings.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDenied = false
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
