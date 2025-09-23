package com.example.secure.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.secure.file.FileManager
import java.io.File

@Composable
fun MoveFileDialog(
    filesToMove: List<FileManager.VaultFile>,
    currentPath: String?,
    allVaultFolders: List<File>,
    onDismiss: () -> Unit,
    onConfirmMove: (destinationFolder: File) -> Unit,
    onCreateFolder: (folderName: String, parentPath: String?, onFolderCreated: (File?) -> Unit) -> Unit
) {
    var selectedDestinationFolder by remember { mutableStateOf<File?>(null) }
    var showCreateNewFolderDialog by remember { mutableStateOf(false) }

    val title = if (filesToMove.size == 1) {
        "Move '${filesToMove.first().file.name}' to..."
    } else {
        "Move ${filesToMove.size} files to..."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = "Select a destination folder in the vault or create a new one:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(allVaultFolders) { folder ->
                        val isSelected = folder == selectedDestinationFolder
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDestinationFolder = folder
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Folder",
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = selectedDestinationFolder?.let { "Selected: ${it.name}" } ?: "No folder selected",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { showCreateNewFolderDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Create New Folder")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDestinationFolder?.let { onConfirmMove(it) }
                },
                enabled = selectedDestinationFolder != null
            ) {
                Text("Move")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showCreateNewFolderDialog) {
        CreateFolderDialog(
            onDismissRequest = { showCreateNewFolderDialog = false },
            onConfirm = { folderName ->
                // Call the onCreateFolder lambda provided by the ViewModel
                onCreateFolder(folderName, currentPath) { createdFolder ->
                    if (createdFolder != null) {
                        selectedDestinationFolder = createdFolder // Select the newly created folder
                    }
                    showCreateNewFolderDialog = false
                }
            }
        )
    }
}