package com.example.secure.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Description // Default document icon
import androidx.compose.material.icons.filled.Image // Default image icon
import androidx.compose.material.icons.filled.Videocam // Default video icon
import androidx.compose.material.icons.filled.Folder // Default folder icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class ViewType {
    LIST, GRID
}

// Placeholder data class for a file/folder item
data class FileSystemItem(
    val id: String,
    val name: String,
    val isFolder: Boolean,
    val mimeType: String? = null, // e.g., "image/jpeg", "video/mp4", "application/pdf"
    val size: Long = 0, // Size in bytes
    val lastModified: Long = 0 // Timestamp
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    item: FileSystemItem,
    viewType: ViewType,
    onClick: (FileSystemItem) -> Unit,
    onView: (FileSystemItem) -> Unit,
    onRename: (FileSystemItem) -> Unit,
    onMove: (FileSystemItem) -> Unit,
    onUnhide: (FileSystemItem) -> Unit,
    onDelete: (FileSystemItem) -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(item) },
                onLongClick = { showContextMenu = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when (viewType) {
            ViewType.LIST -> ListItemView(item, onMenuClick = { showContextMenu = true })
            ViewType.GRID -> GridItemView(item)
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(text = { Text("View") }, onClick = {
                onView(item)
                showContextMenu = false
            })
            DropdownMenuItem(text = { Text("Rename") }, onClick = {
                onRename(item)
                showContextMenu = false
            })
            DropdownMenuItem(text = { Text("Move") }, onClick = {
                onMove(item)
                showContextMenu = false
            })
            DropdownMenuItem(text = { Text("Unhide") }, onClick = {
                onUnhide(item)
                showContextMenu = false
            })
            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                onDelete(item)
                showContextMenu = false
            })
        }
    }
}

@Composable
private fun ListItemView(item: FileSystemItem, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = getIconForItem(item),
                contentDescription = item.name,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(item.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
        }
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
    }
}

@Composable
private fun GridItemView(item: FileSystemItem) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxSize(), // Fill the grid cell
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = getIconForItem(item),
            contentDescription = item.name,
            modifier = Modifier.size(64.dp) // Larger icon for grid view
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(item.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
    }
}

fun getIconForItem(item: FileSystemItem): ImageVector {
    return if (item.isFolder) {
        Icons.Default.Folder
    } else {
        when (item.mimeType?.substringBefore('/')) {
            "image" -> Icons.Default.Image
            "video" -> Icons.Default.Videocam
            else -> Icons.Default.Description // Generic document icon
        }
    }
}
