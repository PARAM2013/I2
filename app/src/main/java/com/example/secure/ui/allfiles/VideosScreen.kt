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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert // For Context Menu
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu // For Context Menu
import androidx.compose.material3.DropdownMenuItem // For Context Menu
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
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
import com.example.secure.ui.composables.RenameItemDialog // Import Rename dialog
import com.example.secure.ui.dashboard.MainDashboardUiState // Required for preview
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import java.io.File // Still needed for File objects within VaultFile/VaultFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadAllVideos()
    }

    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) } // For Rename Dialog
    var showRenameDialog by remember { mutableStateOf(false) } // For Rename Dialog
    var isGridView by remember { mutableStateOf(true) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Videos",
                        style = MaterialTheme.typography.titleMedium // Smaller title for path
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack() // Original popBackStack behavior if already at root
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = stringResource(R.string.action_toggle_view)
                        )
                    }
                }
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
            } else if (uiState.videoFiles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Videos Found",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Add some videos to get started.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                val videoFiles = uiState.videoFiles.sortedByDescending { it.file.lastModified() }

                if (videoFiles.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Videos Found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Add some videos to get started.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(videoFiles, key = { it.file.absolutePath }) { file ->
                                FileItem(
                                    vaultFile = file,
                                    isMenuExpanded = expandedMenuForItemPath == file.file.absolutePath,
                                    onExpandMenu = { expandedMenuForItemPath = file.file.absolutePath },
                                    onDismissMenu = { expandedMenuForItemPath = null },
                                    onUnhideClick = {
                                        viewModel.requestUnhideItem(file)
                                        expandedMenuForItemPath = null // Close menu
                                    },
                                    onDeleteClick = {
                                        // TODO: Show confirmation dialog here before calling delete
                                        viewModel.requestDeleteItem(file)
                                        expandedMenuForItemPath = null // Close menu
                                    },
                                    onRenameClick = {
                                        itemToRename = file
                                        showRenameDialog = true
                                        expandedMenuForItemPath = null // Close menu
                                    },
                                    onClick = {
                                        android.widget.Toast.makeText(context, "File clicked: ${file.file.name}", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    isGridView = isGridView,
                                    onShareClick = {}
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(videoFiles, key = { it.file.absolutePath }) { file ->
                                FileItem(
                                    vaultFile = file,
                                    isMenuExpanded = expandedMenuForItemPath == file.file.absolutePath,
                                    onExpandMenu = { expandedMenuForItemPath = file.file.absolutePath },
                                    onDismissMenu = { expandedMenuForItemPath = null },
                                    onUnhideClick = {
                                        viewModel.requestUnhideItem(file)
                                        expandedMenuForItemPath = null // Close menu
                                    },
                                    onDeleteClick = {
                                        // TODO: Show confirmation dialog here before calling delete
                                        viewModel.requestDeleteItem(file)
                                        expandedMenuForItemPath = null // Close menu
                                    },
                                    onRenameClick = {
                                        itemToRename = file
                                        showRenameDialog = true
                                        expandedMenuForItemPath = null // Close menu
                                    },
                                    onClick = {
                                        android.widget.Toast.makeText(context, "File clicked: ${file.file.name}", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    isGridView = isGridView,
                                    onShareClick = {}
                                )
                                Divider()
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for FAB
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog && itemToRename != null) {
        val currentName = when (itemToRename) {
            is VaultFile -> (itemToRename as VaultFile).file.name
            is VaultFolder -> (itemToRename as VaultFolder).folder.name
            else -> "" // Should not happen if itemToRename is set correctly
        }
        if (currentName.isNotEmpty()) { // Proceed only if we have a valid current name
            RenameItemDialog(
                currentItemName = currentName,
                onDismissRequest = {
                    showRenameDialog = false
                    itemToRename = null
                },
                onConfirm = { newName ->
                    viewModel.requestRenameItem(itemToRename!!, newName)
                    showRenameDialog = false
                    itemToRename = null
                }
            )
        } else { // Reset state if currentName is somehow empty, to avoid showing dialog with no name
            showRenameDialog = false
            itemToRename = null
        }
    }
}
