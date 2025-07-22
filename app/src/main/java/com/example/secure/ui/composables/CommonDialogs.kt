package com.example.secure.ui.composables

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.secure.R
import com.example.secure.ui.theme.ISecureTheme

@OptIn(ExperimentalMaterial3Api::class) // Ensure OptIn is here if AlertDialog or OutlinedTextField are experimental
@Composable
fun CreateFolderDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderNameState by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.create_folder_dialog_title)) },
        text = {
            OutlinedTextField(
                value = folderNameState,
                onValueChange = { folderNameState = it },
                label = { Text(stringResource(R.string.folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderNameState.text.isNotBlank()) {
                        onConfirm(folderNameState.text.trim())
                    } else {
                        Toast.makeText(context, context.getString(R.string.folder_name_empty_error), Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(stringResource(R.string.create_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CreateFolderDialogPreview() {
    ISecureTheme {
        CreateFolderDialog(onDismissRequest = {}, onConfirm = {})
    }
}
