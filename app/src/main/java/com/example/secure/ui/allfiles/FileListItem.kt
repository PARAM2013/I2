package com.example.secure.ui.allfiles

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.util.FileOperations
import com.example.secure.util.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    item: Any,
    viewModel: MainDashboardViewModel,
    isGridView: Boolean,
    onMediaClick: (FileManager.VaultFile) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSelected = uiState.selectedItems.contains(item)
    val context = LocalContext.current

    fun handleItemClick() {
        when (item) {
            is FileManager.VaultFolder -> viewModel.navigateToPath(item.folder.relativeTo(FileManager.getVaultDirectory()).path)
            is FileManager.VaultFile -> {
                if (item.category == FileManager.FileCategory.PHOTO || item.category == FileManager.FileCategory.VIDEO) {
                    onMediaClick(item)
                } else {
                    // Handle document click - open with system app
                    FileOperations.openFile(context, item.file)
                }
            }
        }
    }

    val modifier = if (isGridView) {
        Modifier
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .combinedClickable(
                onClick = {
                    if (uiState.isSelectionModeActive) {
                        viewModel.toggleSelection(item)
                    } else {
                        handleItemClick()
                    }
                },
                onLongClick = {
                    if (!uiState.isSelectionModeActive) {
                        viewModel.enterSelectionMode(item)
                    }
                }
            )
    } else {
        Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .combinedClickable(
                onClick = {
                    if (uiState.isSelectionModeActive) {
                        viewModel.toggleSelection(item)
                    } else {
                        handleItemClick()
                    }
                },
                onLongClick = {
                    if (!uiState.isSelectionModeActive) {
                        viewModel.enterSelectionMode(item)
                    }
                }
            )
    }

    Box(modifier = modifier) {
        when (item) {
            is FileManager.VaultFolder -> FolderItemView(item, isGridView)
            is FileManager.VaultFile -> FileItemView(item, isGridView, viewModel)
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun FolderItemView(folder: FileManager.VaultFolder, isGridView: Boolean) {
    if (isGridView) {
        Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = folder.folder.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            }
        }
    } else {
        ListItem(
            headlineContent = { Text(folder.folder.name) },
            leadingContent = {
                Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(40.dp))
            }
        )
    }
}

@Composable
fun FileItemView(file: FileManager.VaultFile, isGridView: Boolean, viewModel: MainDashboardViewModel) {
    if (isGridView) {
        Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            FileThumbnail(file, Modifier.fillMaxWidth().aspectRatio(1f), viewModel)
        }
    } else {
        ListItem(
            headlineContent = { Text(file.file.name) },
            supportingContent = { Text(text = FileUtils.formatFileSize(file.size)) },
            leadingContent = {
                FileThumbnail(file, Modifier.size(40.dp), viewModel)
            }
        )
    }
}

@Composable
fun FileThumbnail(file: FileManager.VaultFile, modifier: Modifier, viewModel: MainDashboardViewModel) {

    LaunchedEffect(file.file.absolutePath) {
        if (file.thumbnail == null) {
            viewModel.loadThumbnailForItem(file)
        }
    }

    when (file.category) {
        FileManager.FileCategory.PHOTO -> {
            if (file.thumbnail != null) {
                Image(
                    bitmap = file.thumbnail.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_image),
                    contentDescription = null,
                    modifier = modifier
                )
            }
        }
        FileManager.FileCategory.VIDEO -> {
            Box(modifier = modifier) {
                if (file.thumbnail != null) {
                    Image(
                        bitmap = file.thumbnail.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    Icon(
                        Icons.Filled.Videocam,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                Icon(
                    Icons.Filled.Videocam,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
                file.duration?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    )
                }
            }
        }
        FileManager.FileCategory.DOCUMENT -> {
            if (file.thumbnail != null && file.file.extension.lowercase() == "pdf") {
                Box(modifier = modifier) {
                    Image(
                        bitmap = file.thumbnail.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = "PDF",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            } else {
                Icon(
                    painter = painterResource(id = FileManager.getIconForFile(file.file.name)),
                    contentDescription = null,
                    modifier = modifier,
                    tint = Color.Unspecified
                )
            }
        }
        else -> {
            Icon(
                painter = painterResource(id = FileManager.getIconForFile(file.file.name)),
                contentDescription = null,
                modifier = modifier,
                tint = Color.Unspecified
            )
        }
    }
}