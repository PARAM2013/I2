package com.example.secure.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.window.DialogProperties

@Composable
fun MoveSuccessDialog(
    successCount: Int,
    failedCount: Int,
    onDismiss: () -> Unit,
    onViewFiles: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text("Move Operation Complete") },
        text = {
            Column {
                Text("Successfully moved: $successCount files")
                if (failedCount > 0) {
                    Text("Failed to move: $failedCount files")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("You can now view the moved files in their new location.")
            }
        },
        confirmButton = {
            Button(onClick = onViewFiles) {
                Text("View Files")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}