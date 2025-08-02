package com.example.secure.ui.allfiles

import android.app.Activity
import android.app.Application // For Preview ViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult // For FAB
import androidx.activity.result.contract.ActivityResultContracts // For FAB
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add // For FAB
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close // For FAB
import androidx.compose.material.icons.filled.CreateNewFolder // For FAB
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.UploadFile // For FAB
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton // For FAB
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SmallFloatingActionButton // For FAB
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // For FAB state
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.secure.AppGlobalState
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.ui.composables.ConfirmActionDialog
import com.example.secure.ui.composables.CreateFolderDialog
import com.example.secure.ui.composables.RenameItemDialog
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import com.example.secure.ui.viewer.MediaViewerScreen
import com.example.secure.util.SortManager.SortOption
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilesScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val currentPath by viewModel.currentPath.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.fileOperationResult) {
        uiState.fileOperationResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFileOperationResult()
        }
    }

    var itemToRename by remember { mutableStateOf<Any?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var selectedMediaIndex by remember { mutableStateOf<Int?>(null) }
    val mediaFiles = uiState.vaultStats?.allFiles?.filter { it.category == FileManager.FileCategory.PHOTO || it.category == FileManager.FileCategory.VIDEO } ?: emptyList()

    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importFiles(uris)
            }
            isFabMenuExpanded = false
        }
    )

    val deleteRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeletionResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(uiState.deletionPendingIntent) {
        uiState.deletionPendingIntent?.let {
            deleteRequestLauncher.launch(
                androidx.activity.result.IntentSenderRequest.Builder(it.intentSender).build()
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSelectionModeActive) {
                        Text("${uiState.selectedItems.size} selected")
                    } else {
                        Text(
                            text = currentPath?.let { FileManager.VAULT_FOLDER_NAME + File.separator + it } ?: stringResource(R.string.title_all_files),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    if (uiState.isSelectionModeActive) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear Selection")
                        }
                    } else {
                        IconButton(onClick = {
                            if (currentPath != null) {
                                val parentPath = File(currentPath!!).parent
                                viewModel.navigateToPath(parentPath)
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    if (uiState.isSelectionModeActive) {
                        if (uiState.selectedItems.size == 1) {
                            IconButton(onClick = {
                                itemToRename = uiState.selectedItems.first()
                                showRenameDialog = true
                            }) {
                                Icon(Icons.Filled.ModeEdit, contentDescription = "Rename")
                            }
                        }
                        IconButton(onClick = { viewModel.requestUnhideSelectedItems() }) {
                            Icon(Icons.Filled.Visibility, contentDescription = "Unhide")
                        }
                        IconButton(onClick = { viewModel.requestDeleteSelectedItems() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    } else {
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                                contentDescription = stringResource(R.string.action_toggle_view)
                            )
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date: Newest to Oldest") },
                                onClick = { viewModel.setSortOption(SortOption.DATE_DESC); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Date: Oldest to Newest") },
                                onClick = { viewModel.setSortOption(SortOption.DATE_ASC); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Size: Largest to Smallest") },
                                onClick = { viewModel.setSortOption(SortOption.SIZE_DESC); showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Size: Smallest to Largest") },
                                onClick = { viewModel.setSortOption(SortOption.SIZE_ASC); showSortMenu = false }
                            )
                        }
                        IconButton(onClick = {
                            AppGlobalState.isLocked = true
                            activity?.finish()
                        }) {
                            Icon(Icons.Filled.Lock, contentDescription = "Lock App")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (uiState.isSelectionModeActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isSelectionModeActive) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isFabMenuExpanded) {
                        SmallFloatingActionButton(
                            onClick = { filePickerLauncher.launch("*/*") },
                            content = { Icon(Icons.Filled.UploadFile, contentDescription = "Import Files") }
                        )
                        SmallFloatingActionButton(
                            onClick = {
                                viewModel.requestCreateFolderDialog(true)
                                isFabMenuExpanded = false
                                      },
                            content = { Icon(Icons.Filled.CreateNewFolder, contentDescription = "Create Folder") }
                        )
                    }
                    FloatingActionButton(
                        onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                        content = {
                            Icon(
                                imageVector = if (isFabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add,
                                contentDescription = "Toggle FAB Menu"
                            )
                        }
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
            } else {
                val combinedList = (uiState.vaultStats?.allFolders?.sortedBy { it.folder.name.lowercase() } ?: emptyList<Any>()) +
                                     (uiState.vaultStats?.allFiles ?: emptyList())

                if (combinedList.isEmpty()) {
                    EmptyContent()
                } else {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(combinedList, key = { item ->
                                when (item) {
                                    is FileManager.VaultFolder -> item.folder.absolutePath
                                    is FileManager.VaultFile -> item.file.absolutePath
                                    else -> item.hashCode().toString()
                                }
                            }) { item ->
                                FileListItem(
                                    item = item,
                                    viewModel = viewModel,
                                    isGridView = true,
                                    onRename = {
                                        itemToRename = it
                                        showRenameDialog = true
                                    },
                                    onMediaClick = {
                                        selectedMediaIndex = mediaFiles.indexOf(it)
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(combinedList, key = { item ->
                                when (item) {
                                    is FileManager.VaultFolder -> item.folder.absolutePath
                                    is FileManager.VaultFile -> item.file.absolutePath
                                    else -> item.hashCode().toString()
                                }
                            }) { item ->
                                FileListItem(
                                    item = item,
                                    viewModel = viewModel,
                                    isGridView = false,
                                    onRename = {
                                        itemToRename = it
                                        showRenameDialog = true
                                    },
                                    onMediaClick = {
                                        selectedMediaIndex = mediaFiles.indexOf(it)
                                    }
                                )
                                HorizontalDivider()
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateFolderDialog) {
        CreateFolderDialog(
            onDismissRequest = { viewModel.requestCreateFolderDialog(false) },
            onConfirm = { folderName -> viewModel.createFolder(folderName) }
        )
    }

    if (showRenameDialog && itemToRename != null) {
        val currentName = when (val item = itemToRename) {
            is FileManager.VaultFile -> item.file.name
            is FileManager.VaultFolder -> item.folder.name
            else -> ""
        }
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

    if (uiState.showDeleteConfirmation) {
        ConfirmActionDialog(
            title = "Delete Items?",
            message = "Are you sure you want to delete the selected ${uiState.selectedItems.size} items? This action cannot be undone.",
            onConfirm = { viewModel.confirmDeleteSelectedItems() },
            onDismiss = { viewModel.dismissConfirmationDialogs() }
        )
    }

    if (uiState.showUnhideConfirmation) {
        ConfirmActionDialog(
            title = "Unhide Items?",
            message = "Are you sure you want to unhide the selected ${uiState.selectedItems.size} items?",
            onConfirm = { viewModel.confirmUnhideSelectedItems() },
            onDismiss = { viewModel.dismissConfirmationDialogs() }
        )
    }

    if (selectedMediaIndex != null) {
        Dialog(
            onDismissRequest = { selectedMediaIndex = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            MediaViewerScreen(
                files = mediaFiles.map { it.file },
                initialIndex = selectedMediaIndex!!,
                onClose = { selectedMediaIndex = null }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllFilesScreenPreview() {
    ISecureTheme {
        AllFilesScreen(onNavigateBack = {})
    }
}

@Composable
fun EmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = "Empty",
            modifier = Modifier.size(128.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Empty",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}