package com.example.secure.ui.allfiles

import android.app.Application // For Preview ViewModel
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult // For FAB
import androidx.activity.result.contract.ActivityResultContracts // For FAB
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add // For FAB
import androidx.compose.material.icons.filled.Close // For FAB
import androidx.compose.material.icons.filled.CreateNewFolder // For FAB
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert // For Context Menu
import androidx.compose.material.icons.filled.UploadFile // For FAB
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu // For Context Menu
import androidx.compose.material3.DropdownMenuItem // For Context Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton // For FAB
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton // For FAB
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // For FAB state
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.file.FileManager.VaultFile
import com.example.secure.file.FileManager.VaultFolder
import com.example.secure.ui.composables.CreateFolderDialog
import com.example.secure.ui.composables.RenameItemDialog // Import Rename dialog
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import com.example.secure.ui.viewer.MediaViewerScreen
import java.io.File // Still needed for File objects within VaultFile/VaultFolder
import com.example.secure.AppGlobalState
import androidx.compose.material.icons.filled.Lock
import android.app.Activity

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

    var showFabMenu by remember { mutableStateOf(false) }
    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) } // For Rename Dialog
    var showRenameDialog by remember { mutableStateOf(false) } // For Rename Dialog
    var isGridView by remember { mutableStateOf(true) }
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = stringResource(R.string.action_toggle_view)
                        )
                    }
                    IconButton(onClick = {
                        AppGlobalState.isLocked = true
                        activity?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Lock App"
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
                    Log.d("AllFilesScreen", "Folders received: ${folders.size}")
                    combinedList.addAll(folders.sortedBy { it.folder.name.lowercase() })
                }
                uiState.vaultStats?.allFiles?.let { files ->
                    combinedList.addAll(files.sortedBy { it.file.name.lowercase() })
                }

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
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                            viewModel.requestDeleteItem(item)
                                            expandedMenuForItemPath = null // Close menu
                                        },
                                        onRenameClick = {
                                            itemToRename = item
                                            showRenameDialog = true
                                            expandedMenuForItemPath = null // Close menu
                                        },
                                        onClick = {
                                            val folderClickedPath = item.folder.relativeTo(FileManager.getVaultDirectory()).path
                                            viewModel.navigateToPath(folderClickedPath)
                                        },
                                        isGridView = isGridView
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
                                            viewModel.requestDeleteItem(item)
                                            expandedMenuForItemPath = null // Close menu
                                        },
                                        onRenameClick = {
                                            itemToRename = item
                                            showRenameDialog = true
                                            expandedMenuForItemPath = null // Close menu
                                        },
                                        onClick = {
                                            if (item.category == FileManager.FileCategory.PHOTO || item.category == FileManager.FileCategory.VIDEO) {
                                                selectedMediaIndex = mediaFiles.indexOf(item)
                                            } else if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                val file = item.file
                                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.secure.provider", file)
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                intent.setDataAndType(uri, context.contentResolver.getType(uri))
                                                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: android.content.ActivityNotFoundException) {
                                                    android.widget.Toast.makeText(context, "No app found to open this file type.", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        isGridView = isGridView,
                                        onShareClick = {
                                            if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                viewModel.shareFile(item)
                                            }
                                        },
                                        iconResId = FileManager.getIconForFile(item.file.name)
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
                                                viewModel.requestDeleteItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onRenameClick = {
                                                itemToRename = item
                                                showRenameDialog = true
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onClick = {
                                                val folderClickedPath = item.folder.relativeTo(FileManager.getVaultDirectory()).path
                                                viewModel.navigateToPath(folderClickedPath)
                                            },
                                            isGridView = isGridView
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
                                                viewModel.requestDeleteItem(item)
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onRenameClick = {
                                                itemToRename = item
                                                showRenameDialog = true
                                                expandedMenuForItemPath = null // Close menu
                                            },
                                            onClick = {
                                                if (item.category == FileManager.FileCategory.PHOTO || item.category == FileManager.FileCategory.VIDEO) {
                                                    selectedMediaIndex = mediaFiles.indexOf(item)
                                                } else if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                    val file = item.file
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.example.secure.provider", file)
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                    intent.setDataAndType(uri, context.contentResolver.getType(uri))
                                                    intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    try {
                                                        context.startActivity(intent)
                                                    } catch (e: android.content.ActivityNotFoundException) {
                                                        android.widget.Toast.makeText(context, "No app found to open this file type.", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            isGridView = isGridView,
                                            onShareClick = {
                                                if (item.category == FileManager.FileCategory.DOCUMENT) {
                                                    viewModel.shareFile(item)
                                                }
                                            },
                                            iconResId = FileManager.getIconForFile(item.file.name)
                                        )
                                    }
                                }
                                HorizontalDivider()
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
                            
                        )
                    }
                }
            }
        }
    }

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
            else -> ""
        }
        if (currentName.isNotEmpty()) {
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
        } else {
            showRenameDialog = false
            itemToRename = null
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(
    vaultFolder: VaultFolder,
    isMenuExpanded: Boolean,
    onExpandMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onUnhideClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onClick: () -> Unit,
    isGridView: Boolean
) {
    if (isGridView) {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onExpandMenu
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = stringResource(R.string.folder_icon_desc),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = vaultFolder.folder.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }
    } else {
        ListItem(
            headlineContent = { Text(vaultFolder.folder.name) },
            leadingContent = {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = stringResource(R.string.folder_icon_desc),
                    modifier = Modifier.size(40.dp)
                )
            },
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onExpandMenu
            )
        )
    }
    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = onDismissMenu
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.context_menu_unhide)) },
            onClick = onUnhideClick
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.context_menu_rename)) },
            onClick = onRenameClick
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.button_delete)) },
            onClick = onDeleteClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    vaultFile: VaultFile,
    isMenuExpanded: Boolean,
    onExpandMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onUnhideClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onClick: () -> Unit,
    onShareClick: () -> Unit,
    isGridView: Boolean,
    iconResId: Int = R.drawable.ic_file // Default icon
) {
    Box(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onExpandMenu
        )
    ) {
        if (isGridView) {
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Display thumbnail for video, image for photo, and specific icon for documents
                    when (vaultFile.category) {
                        FileManager.FileCategory.VIDEO -> {
                            if (vaultFile.thumbnail != null) {
                                Image(
                                    bitmap = vaultFile.thumbnail.asImageBitmap(),
                                    contentDescription = vaultFile.file.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Videocam,
                                    contentDescription = stringResource(R.string.file_icon_desc),
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }
                        FileManager.FileCategory.PHOTO -> {
                            Image(
                                painter = rememberAsyncImagePainter(model = Uri.fromFile(vaultFile.file)),
                                contentDescription = vaultFile.file.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                        }
                        FileManager.FileCategory.DOCUMENT, FileManager.FileCategory.OTHER -> {
                            Icon(
                                painter = painterResource(id = iconResId),
                                contentDescription = stringResource(R.string.file_icon_desc),
                                modifier = Modifier.size(80.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                    if (vaultFile.category != FileManager.FileCategory.PHOTO && vaultFile.category != FileManager.FileCategory.VIDEO) {
                        Text(
                            text = vaultFile.file.name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        } else {
            ListItem(
                headlineContent = { Text(vaultFile.file.name) },
                supportingContent = { Text(stringResource(R.string.file_size_kb, vaultFile.size / 1024)) },
                leadingContent = {
                    when (vaultFile.category) {
                        FileManager.FileCategory.VIDEO -> {
                            if (vaultFile.thumbnail != null) {
                                Image(
                                    bitmap = vaultFile.thumbnail.asImageBitmap(),
                                    contentDescription = stringResource(R.string.file_icon_desc),
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Videocam,
                                    contentDescription = stringResource(R.string.file_icon_desc),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        FileManager.FileCategory.PHOTO -> {
                            Image(
                                painter = rememberAsyncImagePainter(model = Uri.fromFile(vaultFile.file)),
                                contentDescription = stringResource(R.string.file_icon_desc),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        FileManager.FileCategory.DOCUMENT, FileManager.FileCategory.OTHER -> {
                            Icon(
                                painter = painterResource(id = iconResId),
                                contentDescription = stringResource(R.string.file_icon_desc),
                                modifier = Modifier.size(40.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            )
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.context_menu_unhide)) },
                onClick = onUnhideClick
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.context_menu_rename)) },
                onClick = onRenameClick
            )
            if (vaultFile.category == FileManager.FileCategory.DOCUMENT) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.context_menu_share)) },
                    onClick = onShareClick
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.button_delete)) },
                onClick = onDeleteClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllFilesScreenPreview() {
    ISecureTheme {
        val viewModelForPreview: MainDashboardViewModel = viewModel()
        AllFilesScreen(onNavigateBack = {}, viewModel = viewModelForPreview)
    }
}
