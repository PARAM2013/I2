package com.example.secure.ui.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.secure.R
import com.example.secure.ui.theme.ISecureTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmActionDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = stringResource(id = R.string.button_confirm), // Default to a generic "Confirm"
    dismissButtonText: String = stringResource(id = R.string.button_cancel) // Default to "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismissRequest() // Typically dismiss after confirm
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error // Example: Destructive actions might use error color
                )
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(dismissButtonText)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ConfirmActionDialogPreviewDelete() {
    ISecureTheme {
        ConfirmActionDialog(
            onDismissRequest = {},
            onConfirm = {},
            title = "Confirm Delete",
            message = "Are you sure you want to delete 'MyFile.txt'? This action cannot be undone.",
            confirmButtonText = "Delete" // Specific text for delete
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmActionDialogPreviewGeneric() {
    ISecureTheme {
        ConfirmActionDialog(
            onDismissRequest = {},
            onConfirm = {},
            title = "Confirm Action",
            message = "Are you sure you want to proceed with this action?"
            // Uses default "Confirm" and "Cancel" button texts
        )
    }
}

// Required string resources (add to strings.xml if not present):
// R.string.button_confirm = "Confirm"
// R.string.button_cancel = "Cancel" (already exists)
