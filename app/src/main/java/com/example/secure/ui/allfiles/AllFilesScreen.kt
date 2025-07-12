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
import androidx.compose.material.icons.filled.MoreVert // For Context Menu
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
import com.example.secure.ui.dashboard.MainDashboardUiState
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import java.io.File
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.secure.MainActivity // For NavRoutes
import com.example.secure.ui.dashboard.NavigationEvent
import java.net.URLEncoder
import kotlin.text.Charsets
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.compose.material.icons.filled.Movie // For video icon
import androidx.compose.material.icons.filled.Image // For photo icon
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilesScreen(
    onNavigateBack: () -> Unit, // Kept for explicit back from root, though VM handles path changes
    navController: NavController, // Added NavController
    viewModel: MainDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigateTo.collectAsState()
    val context = LocalContext.current
    val currentPath by viewModel.currentPath.collectAsState()

    var showFabMenu by remember { mutableStateOf(false) }
    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) } // For Rename Dialog
    var showRenameDialog by remember { mutableStateOf(false) } // For Rename Dialog

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
                                            viewModel.requestDeleteItem(item)
                                            expandedMenuForItemPath = null // Close menu
                                        },
                                        onRenameClick = {
                                            itemToRename = item
                                            showRenameDialog = true
                                            expandedMenuForItemPath = null // Close menu
                                        },
                                        onClick = {
                                            viewModel.onFileClicked(item)
                                        }
                                    )
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

@Composable
fun FolderItem(
    vaultFolder: VaultFolder,
    isMenuExpanded: Boolean,
    onExpandMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onUnhideClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
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
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.context_menu_description))
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_unhide)) }, onClick = onUnhideClick)
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_rename)) }, onClick = onRenameClick)
                    DropdownMenuItem(text = { Text(stringResource(R.string.button_delete)) }, onClick = onDeleteClick)
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
    onRenameClick: () -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(vaultFile.file.name) },
        supportingContent = { Text(stringResource(R.string.file_size_kb, vaultFile.size / 1024)) },
        leadingContent = {
            when (vaultFile.category) {
                FileManager.FileCategory.PHOTO -> AsyncImage(
                    model = vaultFile.file,
                    contentDescription = stringResource(R.string.image_thumbnail_desc),
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_placeholder_image),
                    error = painterResource(id = R.drawable.ic_error_image)
                )
                FileManager.FileCategory.VIDEO -> AsyncImage(
                    model = vaultFile.file,
                    contentDescription = stringResource(R.string.video_thumbnail_desc),
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_placeholder_video),
                    error = painterResource(id = R.drawable.ic_error_video)
                )
                else -> Icon(
                    Icons.Filled.Description,
                    contentDescription = stringResource(R.string.file_icon_desc),
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        trailingContent = {
            Box {
                IconButton(onClick = onExpandMenu) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.context_menu_description))
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_unhide)) }, onClick = onUnhideClick)
                    DropdownMenuItem(text = { Text(stringResource(R.string.context_menu_rename)) }, onClick = onRenameClick)
                    DropdownMenuItem(text = { Text(stringResource(R.string.button_delete)) }, onClick = onDeleteClick)
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
        val previewApplication = LocalContext.current.applicationContext as Application
        val viewModelForPreview = MainDashboardViewModel(previewApplication)
        val navController = androidx.navigation.compose.rememberNavController()
        AllFilesScreen(onNavigateBack = {}, viewModel = viewModelForPreview, navController = navController)
    }
}
