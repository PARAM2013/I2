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
import com.example.secure.R
import com.example.secure.file.FileManager // Required for VaultStats, VaultFile, VaultFolder
import com.example.secure.file.FileManager.VaultFile // Explicit import
import com.example.secure.file.FileManager.VaultFolder // Explicit import
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
                        text = stringResource(R.string.empty_vault_all_files_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.empty_vault_all_files_message),
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

                if (combinedList.isEmpty()) { // Handles case where vaultStats is not null but lists are empty
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_vault_all_files_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(R.string.empty_vault_all_files_message),
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
                                        android.widget.Toast.makeText(context, "Folder clicked: ${item.folder.name}", android.widget.Toast.LENGTH_SHORT).show()
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
