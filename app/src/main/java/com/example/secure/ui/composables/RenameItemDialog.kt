package com.example.secure.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.secure.R
import com.example.secure.ui.theme.VaultTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameItemDialog(
    currentItemName: String,
    onDismissRequest: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var newNameState by remember {
        mutableStateOf(TextFieldValue(text = currentItemName, selection = TextRange(0, currentItemName.length)))
    }
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.rename_dialog_title)) }, // Add string R.string.rename_dialog_title
        text = {
            Column {
                Text(stringResource(R.string.rename_dialog_message, currentItemName)) // Add string R.string.rename_dialog_message
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newNameState,
                    onValueChange = { newNameState = it },
                    label = { Text(stringResource(R.string.new_name_hint)) }, // Add string R.string.new_name_hint
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedNewName = newNameState.text.trim()
                    if (trimmedNewName.isNotEmpty() && trimmedNewName != currentItemName) {
                        onConfirm(trimmedNewName)
                        onDismissRequest()
                    } else if (trimmedNewName.isEmpty()) {
                        android.widget.Toast.makeText(context, context.getString(R.string.folder_name_empty_error), android.widget.Toast.LENGTH_SHORT).show() // Reusing existing string
                    } else { // Name is the same or only whitespace changed
                        onDismissRequest() // Just dismiss if name hasn't effectively changed
                    }
                }
            ) {
                Text(stringResource(R.string.rename_button_text)) // Add string R.string.rename_button_text (e.g., "Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.button_cancel)) // Uses existing "Cancel"
            }
        }
    )

    // Request focus and select all text when dialog appears
    LaunchedEffect(Unit) {
        delay(100) // Slight delay to ensure dialog is composed and focusable
        focusRequester.requestFocus()
        // To select all text:
        newNameState = newNameState.copy(selection = TextRange(0, newNameState.text.length))
    }
}

@Preview(showBackground = true)
@Composable
fun RenameItemDialogPreview() {
    VaultTheme {
        RenameItemDialog(
            currentItemName = "MyOldFileName.txt",
            onDismissRequest = {},
            onConfirm = {}
        )
    }
}
