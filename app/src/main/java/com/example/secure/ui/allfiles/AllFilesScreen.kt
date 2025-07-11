package com.example.secure.ui.allfiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.secure.R
import com.example.secure.ui.theme.ISecureTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.secure.ui.dashboard.MainDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilesScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel() // Added ViewModel
) {
    val uiState by viewModel.uiState.collectAsState() // Collect UI state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_all_files)) }, // Assume R.string.title_all_files exists
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back) // Assume R.string.action_back exists
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 16.dp), // Replaced placeholder with direct value
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import com.example.secure.file.FileManager // Required for VaultStats
import java.io.File // Required for File type in VaultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilesScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current // For Toasts or other context needs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_all_files)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                        text = "Vault is Empty",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Add some files or folders to see them here.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val combinedList = mutableListOf<Any>()
                uiState.vaultStats?.allFolders?.let { folders -> combinedList.addAll(folders.sortedBy { it.name.lowercase() }) }
                uiState.vaultStats?.allFiles?.let { files -> combinedList.addAll(files.sortedBy { it.name.lowercase() }) }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(combinedList) { item ->
                        when (item) {
                            is File -> { // Assuming allFolders and allFiles in VaultStats are List<File>
                                if (item.isDirectory) {
                                    FolderItem(folder = item, onClick = {
                                        // TODO: Implement folder click (navigate into folder)
                                        android.widget.Toast.makeText(context, "Folder clicked: ${item.name}", android.widget.Toast.LENGTH_SHORT).show()
                                    })
                                } else {
                                    FileItem(file = item, onClick = {
                                        // TODO: Implement file click (open file or show options)
                                        android.widget.Toast.makeText(context, "File clicked: ${item.name}", android.widget.Toast.LENGTH_SHORT).show()
                                    })
                                }
                            }
                        }
                        Divider()
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // Add some padding at the bottom for FAB
                }
            }
        }
    }
}

@Composable
fun FolderItem(folder: File, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(folder.name) },
        leadingContent = {
            Icon(
                Icons.Filled.Folder,
                contentDescription = "Folder icon",
                modifier = Modifier.size(40.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun FileItem(file: File, onClick: () -> Unit) {
    // Here you could add more logic to determine file type and show specific icons
    ListItem(
        headlineContent = { Text(file.name) },
        supportingContent = { Text("${file.length() / 1024} KB") }, // Example: show size
        leadingContent = {
            Icon(
                Icons.Filled.Description, // Generic file icon
                contentDescription = "File icon",
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
        // Create a fake ViewModel for preview that provides some VaultStats
        val fakeViewModel = object : MainDashboardViewModel(Application()) {
            init {
                val fakeFolders = listOf(File("/fake/folderA"), File("/fake/folderB"))
                val fakeFiles = listOf(File("/fake/file1.txt").apply { createNewFile() }, File("/fake/image.jpg").apply { createNewFile() })
                // Note: File operations like createNewFile() won't work in a real preview environment this way.
                // This is illustrative. Better to mock VaultStats directly.
                _uiState.value = MainDashboardUiState(
                    isLoading = false,
                    vaultStats = FileManager.VaultStats(
                        allFiles = fakeFiles,
                        allFolders = fakeFolders,
                        grandTotalFiles = 2,
                        grandTotalFolders = 2,
                        grandTotalSize = 102400, // Example size
                        // ... other stats can be zero or example values
                        totalPhotoFiles = 1, totalPhotoSize = 51200,
                        totalVideoFiles = 0, totalVideoSize = 0,
                        totalDocumentFiles = 1, totalDocumentSize = 51200,
                        totalOtherFiles = 0, totalOtherSize = 0
                    )
                )
            }
        }
        AllFilesScreen(onNavigateBack = {}, viewModel = fakeViewModel)
    }
}

// Placeholder strings - ensure these are in your strings.xml
// R.string.title_all_files = "All Files"
// R.string.action_back = "Back"

// Removed the common.ui.theme.Padding placeholder as it's not used here anymore.
