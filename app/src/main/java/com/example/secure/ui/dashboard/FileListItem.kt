package com.example.secure.ui.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    vaultFile: Any,
    isGridView: Boolean,
    isMenuExpanded: Boolean,
    onExpandMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onUnhideClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isGridView) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when (file.type) {
                        FileType.FOLDER -> Icons.Default.Folder
                        FileType.IMAGE -> Icons.Default.Image
                        FileType.VIDEO -> Icons.Default.Videocam
                        FileType.DOCUMENT -> Icons.Default.Description
                        FileType.OTHER -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = file.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (file.type) {
                        FileType.FOLDER -> Icons.Default.Folder
                        FileType.IMAGE -> Icons.Default.Image
                        FileType.VIDEO -> Icons.Default.Videocam
                        FileType.DOCUMENT -> Icons.Default.Description
                        FileType.OTHER -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = file.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View") },
                        onClick = {
                            onView(file)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Unhide") },
                        onClick = {
                            onUnhide(file)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete(file)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileListItemPreview() {
    MaterialTheme {
        Column {
            FileItem(
                vaultFile = VaultFile("My Secret Document.pdf", false, FileType.DOCUMENT),
                isGridView = false,
                isMenuExpanded = false,
                onExpandMenu = {},
                onDismissMenu = {},
                onUnhideClick = {},
                onDeleteClick = {},
                onRenameClick = {},
                onShareClick = {},
                onClick = {}
            )
            FileItem(
                vaultFile = VaultFile("Vacation Photos", true, FileType.FOLDER),
                isGridView = true,
                isMenuExpanded = false,
                onExpandMenu = {},
                onDismissMenu = {},
                onUnhideClick = {},
                onDeleteClick = {},
                onRenameClick = {},
                onShareClick = {},
                onClick = {}
            )
            FileItem(
                vaultFile = VaultFile("My_Awesome_Video_of_Cats_Playing_in_the_Garden.mp4", false, FileType.VIDEO),
                isGridView = false,
                isMenuExpanded = false,
                onExpandMenu = {},
                onDismissMenu = {},
                onUnhideClick = {},
                onDeleteClick = {},
                onRenameClick = {},
                onShareClick = {},
                onClick = {}
            )
        }
    }
}
