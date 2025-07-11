package com.example.secure.ui.allfiles

import android.app.Application // For Preview ViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult // For FAB
import androidx.activity.result.contract.ActivityResultContracts // For FAB
import androidx.compose.material.icons.filled.Add // For FAB
import androidx.compose.material.icons.filled.Close // For FAB
import androidx.compose.material.icons.filled.CreateNewFolder // For FAB
import androidx.compose.material.icons.filled.UploadFile // For FAB
import androidx.compose.material3.FloatingActionButton // For FAB
import androidx.compose.material3.SmallFloatingActionButton // For FAB
import androidx.compose.runtime.mutableStateOf // For FAB state
import androidx.compose.runtime.remember // For FAB state
import androidx.compose.runtime.setValue // For FAB state
import com.example.secure.R
import com.example.secure.file.FileManager // Required for VaultStats, VaultFile, VaultFolder
import com.example.secure.file.FileManager.VaultFile // Explicit import
import com.example.secure.file.FileManager.VaultFolder // Explicit import
import com.example.secure.ui.composables.CreateFolderDialog // Import the extracted dialog
import com.example.secure.ui.dashboard.MainDashboardUiState // Required for preview
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import java.io.File // Still needed for File objects within VaultFile/VaultFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilesScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentPath by viewModel.currentPath.collectAsState()

    var showFabMenu by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importFiles(uris) // ViewModel's importFiles uses currentPath
            }
            showFabMenu = false // Close menu after selection or cancellation
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentPath?.let { FileManager.VAULT_FOLDER_NAME + File.separator + it } ?: stringResource(R.string.title_all_files),
                        style = MaterialTheme.typography.titleMedium // Smaller title for path
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val parentPath = currentPath?.let { File(it).parent }
                        if (parentPath != null || currentPath != null) { // If currentPath is not null, parentPath could be null (at root)
                            viewModel.navigateToPath(parentPath)
                        } else {
                            onNavigateBack() // Original popBackStack behavior if already at root
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showFabMenu) {
                    SmallFloatingActionButton(
                        onClick = {
                            viewModel.requestCreateFolderDialog(true)
                            showFabMenu = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Filled.CreateNewFolder, stringResource(R.string.fab_create_folder))
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(Icons.Filled.UploadFile, stringResource(R.string.fab_import_file))
                    }
                }
                FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = stringResource(R.string.fab_options_toggle)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.vaultStats == null || (uiState.vaultStats!!.allFiles.isEmpty() && uiState.vaultStats!!.allFolders.isEmpty())) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_folder_title), // New string: "Folder is Empty"
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.empty_folder_message), // New string: "This folder has no items."
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val combinedList = mutableListOf<Any>() // Now stores VaultFolder or VaultFile
                uiState.vaultStats?.allFolders?.let { folders ->
                    combinedList.addAll(folders.sortedBy { it.folder.name.lowercase() })
                }
                uiState.vaultStats?.allFiles?.let { files ->
                    combinedList.addAll(files.sortedBy { it.file.name.lowercase() })
                }

                // This explicit isEmpty check after combining might be redundant if the above condition for empty state is robust.
                // However, keeping it for safety in case vaultStats is not null but lists are empty.
                if (combinedList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_folder_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(R.string.empty_folder_message),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(combinedList, key = { item ->
                            when (item) {
                                is VaultFolder -> item.folder.absolutePath
                                is VaultFile -> item.file.absolutePath
                                else -> item.hashCode().toString() // Fallback key
                            }
                        }) { item ->
                            when (item) {
                                is VaultFolder -> {
                                    FolderItem(vaultFolder = item, onClick = {
                                        // Construct new path relative to vault root
                                        val folderClickedPath = item.folder.relativeTo(FileManager.getVaultDirectory()).path
                                        viewModel.navigateToPath(folderClickedPath)
                                    })
                                }
                                is VaultFile -> {
                                    FileItem(vaultFile = item, onClick = {
                                        android.widget.Toast.makeText(context, "File clicked: ${item.file.name}", android.widget.Toast.LENGTH_SHORT).show()
                                    })
                                }
                            }
                            Divider()
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for FAB
                    }
                }
            }
        }
    }

    // Conditionally display the CreateFolderDialog
    // It's triggered by viewModel.requestCreateFolderDialog(true)
    // The ViewModel then sets uiState.showCreateFolderDialog
    if (uiState.showCreateFolderDialog) {
        CreateFolderDialog(
            onDismissRequest = { viewModel.requestCreateFolderDialog(false) },
            onConfirm = { folderName ->
                viewModel.createFolder(folderName)
            }
        )
    }
}

@Composable
fun FolderItem(vaultFolder: VaultFolder, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(vaultFolder.folder.name) },
        leadingContent = {
            Icon(
                Icons.Filled.Folder,
                contentDescription = stringResource(R.string.folder_icon_desc),
                modifier = Modifier.size(40.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun FileItem(vaultFile: VaultFile, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(vaultFile.file.name) },
        supportingContent = { Text(stringResource(R.string.file_size_kb, vaultFile.size / 1024)) },
        leadingContent = {
            // TODO: Could use vaultFile.category to show different icons
            Icon(
                Icons.Filled.Description, // Generic file icon
                contentDescription = stringResource(R.string.file_icon_desc),
                modifier = Modifier.size(40.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
fun AllFilesScreenPreview() {
    ISecureTheme {
        // To fix compilation errors related to anonymous subclassing a final class
        // and accessing private state, we directly instantiate MainDashboardViewModel.
        // The preview will show the ViewModel's state after its init block runs.
        // For a data-rich preview with specific VaultStats, MainDashboardViewModel
        // would need to be designed for easier state injection in previews (e.g., via constructor or a test helper).
        val previewApplication = LocalContext.current.applicationContext as Application
        val viewModelForPreview = MainDashboardViewModel(previewApplication)

        // You could potentially create a more elaborate FakeMainDashboardViewModel if needed,
        // but this direct instantiation fixes the compile errors.
        // If you want to show specific data in preview without modifying the actual ViewModel,
        // you would typically pass a manually constructed MainDashboardUiState to a modified AllFilesScreen
        // that can accept UiState directly for preview purposes, or use a testing library for ViewModel mocking.

        AllFilesScreen(onNavigateBack = {}, viewModel = viewModelForPreview)
    }
}
