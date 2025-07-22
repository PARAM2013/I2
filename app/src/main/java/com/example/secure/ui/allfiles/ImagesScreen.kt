package com.example.secure.ui.allfiles

import android.app.Application
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.LinearProgressIndicator
import com.example.secure.ui.viewmodel.FileOperationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel(),
    fileOperationsViewModel: FileOperationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val fileOpState = fileOperationsViewModel.state
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val fileListState by viewModel.fileListState.collectAsState()


    // Launch a coroutine to load all images when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadAllImages()
    }

    val imageFiles = uiState.imageFiles.sortedByDescending { it.file.lastModified() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Images",
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
            if (fileOpState.isProcessing) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = fileOpState.progress,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                    Text(
                        "${fileOpState.processedFiles}/${fileOpState.totalFiles} files processed",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (imageFiles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Images Found",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Add some images to get started.",
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
                        items(imageFiles, key = { it.file.absolutePath }) { file ->
                            FileItem(
                                vaultFile = file,
                                isSelected = file.file in fileListState.selectedFiles,
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
                                onLongClick = {
                                    viewModel.enterSelectionMode()
                                    viewModel.toggleSelection(file.file)
                                },
                                onClick = {
                                    if (fileListState.isSelectionMode) {
                                        viewModel.toggleSelection(file.file)
                                    } else {
                                        selectedImageIndex = imageFiles.indexOf(file)
                                    }
                                },
                                isGridView = isGridView,
                                onShareClick = {}
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(imageFiles, key = { it.file.absolutePath }) { file ->
                            FileItem(
                                vaultFile = file,
                                isSelected = file.file in fileListState.selectedFiles,
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
                                onLongClick = {
                                    viewModel.enterSelectionMode()
                                    viewModel.toggleSelection(file.file)
                                },
                                onClick = {
                                    if (fileListState.isSelectionMode) {
                                        viewModel.toggleSelection(file.file)
                                    } else {
                                        selectedImageIndex = imageFiles.indexOf(file)
                                    }
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
    if (selectedImageIndex != null) {
        Dialog(
            onDismissRequest = { selectedImageIndex = null },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            MediaViewerScreen(
                files = imageFiles.map { it.file },
                initialIndex = selectedImageIndex!!,
                onClose = { selectedImageIndex = null },
                onDelete = { file ->
                    viewModel.requestDeleteItem(imageFiles.find { it.file == file }!!)
                    selectedImageIndex = null
                },
                onUnhide = { file ->
                    viewModel.requestUnhideItem(imageFiles.find { it.file == file }!!)
                    selectedImageIndex = null
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
