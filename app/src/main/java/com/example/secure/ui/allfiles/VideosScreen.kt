package com.example.secure.ui.allfiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.secure.R
import com.example.secure.file.FileManager.VaultFile
import com.example.secure.file.FileManager.VaultFolder
import com.example.secure.ui.composables.RenameItemDialog
import com.example.secure.ui.dashboard.MainDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    onNavigateBack: () -> Unit,
    onVideoClick: (Int) -> Unit,
    viewModel: MainDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    val videoFiles = uiState.videoFiles.sortedByDescending { it.file.lastModified() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Videos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = stringResource(R.string.action_toggle_view)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (videoFiles.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No Videos Found", style = MaterialTheme.typography.headlineSmall)
                    Text("Add some videos to get started.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(videoFiles, key = { _, item -> item.file.absolutePath }) { index, file ->
                            FileItem(
                                vaultFile = file,
                                isGridView = isGridView,
                                isMenuExpanded = expandedMenuForItemPath == file.file.absolutePath,
                                onExpandMenu = { expandedMenuForItemPath = file.file.absolutePath },
                                onDismissMenu = { expandedMenuForItemPath = null },
                                onRenameClick = {
                                    itemToRename = file
                                    showRenameDialog = true
                                    expandedMenuForItemPath = null
                                },
                                onDeleteClick = {
                                    viewModel.requestDeleteItem(file)
                                    expandedMenuForItemPath = null
                                },
                                onUnhideClick = {
                                    viewModel.requestUnhideItem(file)
                                    expandedMenuForItemPath = null
                                },
                                onShareClick = { /* Not implemented for videos */ },
                                onClick = { onVideoClick(index) }
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(videoFiles, key = { _, item -> item.file.absolutePath }) { index, file ->
                            FileItem(
                                vaultFile = file,
                                isGridView = isGridView,
                                isMenuExpanded = expandedMenuForItemPath == file.file.absolutePath,
                                onExpandMenu = { expandedMenuForItemPath = file.file.absolutePath },
                                onDismissMenu = { expandedMenuForItemPath = null },
                                onRenameClick = {
                                    itemToRename = file
                                    showRenameDialog = true
                                    expandedMenuForItemPath = null
                                },
                                onDeleteClick = {
                                    viewModel.requestDeleteItem(file)
                                    expandedMenuForItemPath = null
                                },
                                onUnhideClick = {
                                    viewModel.requestUnhideItem(file)
                                    expandedMenuForItemPath = null
                                },
                                onShareClick = { /* Not implemented for videos */ },
                                onClick = { onVideoClick(index) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog && itemToRename != null) {
        val currentName = when (val item = itemToRename) {
            is VaultFile -> item.file.name
            is VaultFolder -> item.folder.name
            else -> ""
        }
        if (currentName.isNotEmpty()) {
            RenameItemDialog(
                currentItemName = currentName,
                onDismissRequest = { showRenameDialog = false; itemToRename = null },
                onConfirm = { newName ->
                    viewModel.requestRenameItem(itemToRename!!, newName)
                    showRenameDialog = false
                    itemToRename = null
                }
            )
        }
    }
}
