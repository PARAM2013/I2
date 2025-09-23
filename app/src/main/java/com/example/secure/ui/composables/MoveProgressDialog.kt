package com.example.secure.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.secure.ui.dashboard.MoveProgress
import androidx.compose.foundation.layout.fillMaxWidth // Added missing import

@Composable
fun MoveProgressDialog(
    moveProgress: MoveProgress,
    onCancel: () -> Unit
) {
    if (moveProgress.isMoving) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss during move */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text("Moving Files...") },
            text = {
                Column {
                    Text(
                        "Moving ${moveProgress.currentFileIndex} of ${moveProgress.totalFiles} files.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Current: ${moveProgress.currentFileName}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = moveProgress.overallProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Success: ${moveProgress.successfulMoves}, Failed: ${moveProgress.failedMoves}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                if (moveProgress.canCancel) {
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}