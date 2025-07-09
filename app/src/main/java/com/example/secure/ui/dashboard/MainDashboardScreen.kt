package com.example.secure.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
// import android.net.Uri // Not directly used here, but MainDashboardViewModel uses it.
// import com.example.secure.ui.dashboard.VaultFile // Already implicitly used via viewModel
// import com.example.secure.ui.dashboard.FileType // Already implicitly used via viewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    onSettingsClick: () -> Unit,
    onImportFile: () -> Unit,
    onImportFolder: () -> Unit, // Keep for FAB, though full implementation might be complex
    onCreateFolder: (String) -> Unit, // Used by FAB -> Dialog -> ViewModel
    viewModel: MainDashboardViewModel = viewModel()
) {
    var isGridView by remember { mutableStateOf(false) }
    val vaultFiles by viewModel.vaultFiles.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    // Handle system back button for navigation
    BackHandler(enabled = currentPath.isNotEmpty()) {
        viewModel.navigateUp()
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onConfirm = { folderName ->
                viewModel.createFolder(folderName) // Call ViewModel directly
                showCreateFolderDialog = false
            },
            onDismiss = { showCreateFolderDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentPath.isEmpty()) "iSecure" else "iSecure / ${currentPath.replace(File.separatorChar, '/')}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (currentPath.isNotEmpty()) {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate Up"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.ViewModule,
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
                onImportFile = onImportFile,
                onImportFolder = onImportFolder, // This still calls the MainActivity lambda
                onCreateFolder = { showCreateFolderDialog = true }
            )
        }
    ) { paddingValues ->
        if (vaultFiles.isEmpty()) {
            EmptyVaultState(modifier = Modifier.padding(paddingValues))
        } else {
            val fileClickHandler: (VaultFile) -> Unit = { file ->
                if (file.isFolder) {
                    viewModel.navigateTo(file.name)
                } else {
                    // TODO: Handle actual file click (e.g., open, view details)
                    // For now, perhaps a Toast or log
                    // Toast.makeText(context, "Clicked on file: ${file.name}", Toast.LENGTH_SHORT).show()
                    println("Clicked on file: ${file.name}")
                }
            }

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
                    items(vaultFiles, key = { it.name + it.isFolder }) { file -> // Added key for stability
                        FileListItem(
                            file = file,
                            isGridView = true,
                            onFileClick = fileClickHandler,
                            onFileLongClick = { /* TODO: Handle file long click */ },
                            onView = { /* TODO: Handle view action */ },
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
                    items(vaultFiles, key = { it.name + it.isFolder }) { file -> // Added key for stability
                        FileListItem(
                            file = file,
                            isGridView = false,
                            onFileClick = fileClickHandler,
                            onFileLongClick = { /* TODO: Handle file long click */ },
                            onView = { /* TODO: Handle view action */ },
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
    onCreateFolder: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 200)
    )

    Column(horizontalAlignment = Alignment.End) {
        if (expanded) {
            SmallFabItem(text = "Create Folder", icon = Icons.Default.CreateNewFolder) { onCreateFolder(); expanded = false }
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
fun CreateFolderDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Folder") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Folder Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(folderName) },
                enabled = folderName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
            onImportFile = {},
            onImportFolder = {},
            onCreateFolder = {}
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
            onCreateFolder = {}
        )
    }
}
