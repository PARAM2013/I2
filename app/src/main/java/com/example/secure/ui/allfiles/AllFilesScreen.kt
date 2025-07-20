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
import android.net.Uri
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.file.FileManager.VaultFile
import com.example.secure.file.FileManager.VaultFolder
import com.example.secure.ui.composables.CreateFolderDialog
import com.example.secure.ui.composables.RenameItemDialog // Import Rename dialog
import com.example.secure.ui.dashboard.MainDashboardUiState // Required for preview
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import com.example.secure.ui.viewer.MediaViewerScreen
import java.io.File // Still needed for File objects within VaultFile/VaultFolder
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) } // For Rename Dialog
    var showRenameDialog by remember { mutableStateOf(false) } // For Rename Dialog
    var isGridView by remember { mutableStateOf(false) }
    var selectedMediaIndex by remember { mutableStateOf<Int?>(null) }
    val mediaFiles = uiState.vaultStats?.allFiles?.filter { it.category == FileManager.FileCategory.PHOTO || it.category == FileManager.FileCategory.VIDEO } ?: emptyList()

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
                        if (currentPath != null) {
                            val parentPath = File(currentPath!!).parent
                            viewModel.navigateToPath(parentPath)
                        } else {
                            onNavigateBack()
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
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(combinedList, key = { item ->
                                when (item) {
                                    is VaultFolder -> item.folder.absolutePath
                                    is VaultFile -> item.file.absolutePath
                                    else -> item.hashCode().toString() // Fallback key
                                }
                            }) { item ->
                                when (item) {
                                    is VaultFolder -> {
                                        FolderItem(
                                            vaultFolder = item,
                                            isMenuExpanded = expandedMenuForItemPath == item.folder.absolutePath,
                                            onExpandMenu = { expandedMenuForItemPath = item.folder.absolutePath },
                                            onDismissMenu = { expandedMenuForItemPath = null },
                                            onUnhideClick = {
                                                viewModel.requestUnhideItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onDeleteClick = {
                                                // TODO: Show confirmation dialog here before calling delete
                                                viewModel.requestDeleteItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onRenameClick = {
                                                itemToRename = item
                                                showRenameDialog = true
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onClick = {
                                                // Construct new path relative to vault root
                                                val folderClickedPath = item.folder.relativeTo(FileManager.getVaultDirectory()).path
                                                viewModel.navigateToPath(folderClickedPath)
                                            }
                                        )
                                    }
                                    is VaultFile -> {
                                        FileItem(
                                            vaultFile = item,
                                            isMenuExpanded = expandedMenuForItemPath == item.file.absolutePath,
                                            onExpandMenu = { expandedMenuForItemPath = item.file.absolutePath },
                                            onDismissMenu = { expandedMenuForItemPath = null },
                                            onUnhideClick = {
                                                viewModel.requestUnhideItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onDeleteClick = {
                                                // TODO: Show confirmation dialog here before calling delete
                                                viewModel.requestDeleteItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onRenameClick = {
                                                itemToRename = item
                                                showRenameDialog = true
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onClick = {
                                                val file = item.file
                                                if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.secure.provider", file)
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                    intent.setDataAndType(uri, context.contentResolver.getType(uri))
                                                    intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    try {
                                                        context.startActivity(intent)
                                                    } catch (e: android.content.ActivityNotFoundException) {
                                                        android.widget.Toast.makeText(context, "No app found to open this file type.", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                } else if (item.category == FileManager.FileCategory.PHOTO || item.category == FileManager.FileCategory.VIDEO) {
                                                    selectedMediaIndex = mediaFiles.indexOf(item)
                                                }
                                            },
                                            isGridView = isGridView,
                                            onShareClick = {
                                                if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                    viewModel.shareFile(item)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
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
                                        FolderItem(
                                            vaultFolder = item,
                                            isMenuExpanded = expandedMenuForItemPath == item.folder.absolutePath,
                                            onExpandMenu = { expandedMenuForItemPath = item.folder.absolutePath },
                                            onDismissMenu = { expandedMenuForItemPath = null },
                                            onUnhideClick = {
                                                viewModel.requestUnhideItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onDeleteClick = {
                                                // TODO: Show confirmation dialog here before calling delete
                                                viewModel.requestDeleteItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onRenameClick = {
                                                itemToRename = item
                                                showRenameDialog = true
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onClick = {
                                                // Construct new path relative to vault root
                                                val folderClickedPath = item.folder.relativeTo(FileManager.getVaultDirectory()).path
                                                viewModel.navigateToPath(folderClickedPath)
                                            }
                                        )
                                    }
                                    is VaultFile -> {
                                        FileItem(
                                            vaultFile = item,
                                            isMenuExpanded = expandedMenuForItemPath == item.file.absolutePath,
                                            onExpandMenu = { expandedMenuForItemPath = item.file.absolutePath },
                                            onDismissMenu = { expandedMenuForItemPath = null },
                                            onUnhideClick = {
                                                viewModel.requestUnhideItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onDeleteClick = {
                                                // TODO: Show confirmation dialog here before calling delete
                                                viewModel.requestDeleteItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onRenameClick = {
                                                itemToRename = item
                                                showRenameDialog = true
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onClick = {
                                                val file = item.file
                                                if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.secure.provider", file)
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                    intent.setDataAndType(uri, context.contentResolver.getType(uri))
                                                    intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    try {
                                                        context.startActivity(intent)
                                                    } catch (e: android.content.ActivityNotFoundException) {
                                                        android.widget.Toast.makeText(context, "No app found to open this file type.", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                } else if (item.category == FileManager.FileCategory.PHOTO || item.category == FileManager.FileCategory.VIDEO) {
                                                    selectedMediaIndex = mediaFiles.indexOf(item)
                                                }
                                            },
                                            onShareClick = {
                                                if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                    viewModel.shareFile(item)
                                                }
                                            },
                                            isGridView = isGridView
                                        )
                                    }
                                }
                                Divider()
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for FAB
                        }
                    }
                }

                if (selectedMediaIndex != null) {
                    Dialog(
                        onDismissRequest = { selectedMediaIndex = null },
                        properties = DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = false,
                            usePlatformDefaultWidth = false
                        )
                    ) {
                        MediaViewerScreen(
                            files = mediaFiles.map { it.file },
                            initialIndex = selectedMediaIndex!!,
                            onClose = { selectedMediaIndex = null },
                            onDelete = { file ->
                                viewModel.requestDeleteItem(mediaFiles.find { it.file == file }!!)
                                selectedMediaIndex = null
                            },
                            onUnhide = { file ->
                                viewModel.requestUnhideItem(mediaFiles.find { it.file == file }!!)
                                selectedMediaIndex = null
                            }
                        )
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
                    // Dialog is dismissed and itemToRename reset by RenameItemDialog's onConfirm logic
                    // if successful, or if name is same/empty.
                    // ViewModel will update isLoading and fileOperationResult.
                    // No, RenameItemDialog's onConfirm doesn't dismiss itself if onConfirm is called.
                    // The caller of RenameItemDialog is responsible for dismissing it.
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

@Composable
fun FolderItem(
    vaultFolder: VaultFolder,
    isMenuExpanded: Boolean,
    onExpandMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onUnhideClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit, // New callback
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(vaultFolder.folder.name) },
        leadingContent = {
            Icon(
                Icons.Filled.Folder,
                contentDescription = stringResource(R.string.folder_icon_desc),
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = onExpandMenu) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.context_menu_description)) // TODO: Add string R.string.context_menu_description
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_unhide)) }, onClick = onUnhideClick)
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_rename)) }, onClick = onRenameClick) // New Item
                    DropdownMenuItem(text = { Text(stringResource(R.string.button_delete)) }, onClick = onDeleteClick)
                    // Add other items later
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun FileItem(
    vaultFile: VaultFile,
    isMenuExpanded: Boolean,
    onExpandMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onUnhideClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit, // New callback
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    isGridView: Boolean = false
) {
    ListItem(
        headlineContent = { Text(vaultFile.file.name) },
        supportingContent = { Text(stringResource(R.string.file_size_kb, vaultFile.size / 1024)) },
        leadingContent = {
            val file = vaultFile.file
            val context = LocalContext.current
            val thumbnail = remember(file) {
                when (file.extension.lowercase()) {
                    in listOf("jpg", "jpeg", "png", "gif") -> {
                        Uri.fromFile(file)
                    }
                    in listOf("mp4", "mkv", "webm", "avi", "3gp") -> {
                        val thumbnailBitmap = android.media.ThumbnailUtils.createVideoThumbnail(
                            file.absolutePath,
                            android.provider.MediaStore.Video.Thumbnails.MINI_KIND
                        )
                        thumbnailBitmap
                    }
                    else -> {
                        null
                    }
                }
            }

            if (thumbnail != null) {
                Image(
                    painter = rememberAsyncImagePainter(thumbnail),
                    contentDescription = stringResource(R.string.file_icon_desc),
                    modifier = Modifier.size(if (isGridView) 128.dp else 40.dp)
                )
            } else {
                Icon(
                    when (file.extension.lowercase()) {
                        in listOf("mp4", "mkv", "webm", "avi", "3gp") -> Icons.Filled.Videocam
                        else -> Icons.Filled.Description
                    },
                    contentDescription = stringResource(R.string.file_icon_desc),
                    modifier = Modifier.size(if (isGridView) 128.dp else 40.dp)
                )
            }
        },
        trailingContent = {
            Box {
                IconButton(onClick = onExpandMenu) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.context_menu_description)) // Same string
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_unhide)) }, onClick = onUnhideClick)
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_rename)) }, onClick = onRenameClick) // New Item
                    if (vaultFile.category == FileManager.FileCategory.DOCUMENT) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_share)) }, onClick = onShareClick)
                    }
                    DropdownMenuItem(text = { Text(stringResource(R.string.button_delete)) }, onClick = onDeleteClick)
                    // Add other items later
                }
            }
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
        val viewModelForPreview: MainDashboardViewModel = viewModel()

        // You could potentially create a more elaborate FakeMainDashboardViewModel if needed,
        // but this direct instantiation fixes the compile errors.
        // If you want to show specific data in preview without modifying the actual ViewModel,
        // you would typically pass a manually constructed MainDashboardUiState to a modified AllFilesScreen
        // that can accept UiState directly for preview purposes, or use a testing library for ViewModel mocking.

        AllFilesScreen(onNavigateBack = {}, viewModel = viewModelForPreview)
    }
}
