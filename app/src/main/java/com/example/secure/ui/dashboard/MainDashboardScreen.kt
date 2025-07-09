package com.example.secure.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    onSettingsClick: () -> Unit,
    onImportFile: (Uri) -> Unit,
    onImportFolder: () -> Unit,
    onCreateFolder: (String) -> Unit,
    viewModel: MainDashboardViewModel = viewModel()
) {
    var isGridView by remember { mutableStateOf(false) }
    val vaultFiles by viewModel.vaultFiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("iSecure") },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.ViewModule,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            MultiDirectionalFab(
                onImportFile = { /* TODO: Implement file picker and pass URI */ },
                onImportFolder = onImportFolder,
                onCreateFolder = onCreateFolder
            )
        }
    ) { paddingValues ->
        if (vaultFiles.isEmpty()) {
            EmptyVaultState(modifier = Modifier.padding(paddingValues))
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vaultFiles) { file ->
                        FileListItem(
                            file = file,
                            isGridView = true,
                            onFileClick = { /* TODO: Handle file click */ },
                            onFileLongClick = { /* TODO: Handle file long click */ },
                            onView = { /* TODO: Handle view */ },
                            onUnhide = { viewModel.unhideFile(it) },
                            onDelete = { viewModel.deleteFile(it) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(vaultFiles) { file ->
                        FileListItem(
                            file = file,
                            isGridView = false,
                            onFileClick = { /* TODO: Handle file click */ },
                            onFileLongClick = { /* TODO: Handle file long click */ },
                            onView = { /* TODO: Handle view */ },
                            onUnhide = { viewModel.unhideFile(it) },
                            onDelete = { viewModel.deleteFile(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyVaultState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOff,
            contentDescription = "Vault Empty",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your vault is empty. Tap the '+' button to add files.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun MultiDirectionalFab(
    onImportFile: () -> Unit,
    onImportFolder: () -> Unit,
    onCreateFolder: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    Column(horizontalAlignment = Alignment.End) {
        if (expanded) {
            SmallFabItem(text = "Create Folder", icon = Icons.Default.CreateNewFolder) { onCreateFolder("New Folder"); expanded = false } // Pass a default name for now
            Spacer(modifier = Modifier.height(8.dp))
            SmallFabItem(text = "Import Folder", icon = Icons.Default.FolderOpen) { onImportFolder(); expanded = false }
            Spacer(modifier = Modifier.height(8.dp))
            SmallFabItem(text = "Import File", icon = Icons.Default.FileUpload) { onImportFile(); expanded = false }
            Spacer(modifier = Modifier.height(16.dp))
        }
        FloatingActionButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
fun SmallFabItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        text = { Text(text, fontSize = 12.sp) },
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.height(40.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun MainDashboardScreenPreview() {
    MaterialTheme {
        MainDashboardScreen(
            onSettingsClick = {},
            onImportFile = { _ -> },
            onImportFolder = {},
            onCreateFolder = { _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyVaultStatePreview() {
    MaterialTheme {
        EmptyVaultState()
    }
}

@Preview(showBackground = true)
@Composable
fun MultiDirectionalFabPreview() {
    MaterialTheme {
        MultiDirectionalFab(
            onImportFile = {},
            onImportFolder = {},
            onCreateFolder = { _ -> }
        )
    }
}
