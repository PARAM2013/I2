package com.example.secure.ui.allfiles

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.file.FileManager.VaultFile
import com.example.secure.file.FileManager.VaultFolder
import com.example.secure.ui.composables.RenameItemDialog
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import com.example.secure.ui.viewer.MediaViewerScreen
import java.io.File // Still needed for File objects within VaultFile/VaultFolder
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedVideoIndex by remember { mutableStateOf<Int?>(null) }
    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val videoFiles = uiState.videoFiles.sortedByDescending { it.file.lastModified() }

    LaunchedEffect(Unit) {
        viewModel.loadAllVideos()
    }

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
            } else if (videoFiles.isEmpty()) {
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
                                    selectedVideoIndex = videoFiles.indexOf(file)
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
                                    selectedVideoIndex = videoFiles.indexOf(file)
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

    // Show Media Viewer Dialog
    if (selectedVideoIndex != null) {
        Dialog(
            onDismissRequest = { selectedVideoIndex = null },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            MediaViewerScreen(
                files = videoFiles.map { it.file },
                initialIndex = selectedVideoIndex!!,
                onClose = { selectedVideoIndex = null },
                onDelete = { file ->
                    viewModel.requestDeleteItem(videoFiles.find { it.file == file }!!)
                    selectedVideoIndex = null
                },
                onUnhide = { file ->
                    viewModel.requestUnhideItem(videoFiles.find { it.file == file }!!)
                    selectedVideoIndex = null
                }
            )
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
