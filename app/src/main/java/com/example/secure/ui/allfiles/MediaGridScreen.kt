package com.example.secure.ui.allfiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.ui.dashboard.MainDashboardViewModel

enum class MediaType {
    IMAGE,
    VIDEO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGridScreen(
    mediaType: MediaType,
    onNavigateBack: () -> Unit,
    viewModel: MainDashboardViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(mediaType) {
        when (mediaType) {
            MediaType.IMAGE -> viewModel.loadAllImages()
            MediaType.VIDEO -> viewModel.loadAllVideos()
        }
    }

    var expandedMenuForItemPath by remember { mutableStateOf<String?>(null) }
    var itemToRename by remember { mutableStateOf<Any?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }

    val title = when (mediaType) {
        MediaType.IMAGE -> "Images"
        MediaType.VIDEO -> "Videos"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            } else {
                val mediaFiles = when (mediaType) {
                    MediaType.IMAGE -> uiState.imageFiles
                    MediaType.VIDEO -> uiState.videoFiles
                }.sortedByDescending { it.file.lastModified() }

                if (mediaFiles.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No ${title.lowercase()} found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Add some ${title.lowercase()} to get started.",
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
                            items(mediaFiles, key = { it.file.absolutePath }) { file ->
                                FileItem(
                                    vaultFile = file,
                                    isMenuExpanded = expandedMenuForItemPath == file.file.absolutePath,
                                    onExpandMenu = { expandedMenuForItemPath = file.file.absolutePath },
                                    onDismissMenu = { expandedMenuForItemPath = null },
                                    onUnhideClick = {
                                        viewModel.requestUnhideItem(file)
                                        expandedMenuForItemPath = null
                                    },
                                    onDeleteClick = {
                                        viewModel.requestDeleteItem(file)
                                        expandedMenuForItemPath = null
                                    },
                                    onRenameClick = {
                                        itemToRename = file
                                        showRenameDialog = true
                                        expandedMenuForItemPath = null
                                    },
                                    onClick = {
                                        val index = mediaFiles.indexOf(file)
                                        navController.navigate("media_viewer/${mediaType.name}/$index")
                                    },
                                    isGridView = isGridView,
                                    onShareClick = {}
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(mediaFiles, key = { it.file.absolutePath }) { file ->
                                FileItem(
                                    vaultFile = file,
                                    isMenuExpanded = expandedMenuForItemPath == file.file.absolutePath,
                                    onExpandMenu = { expandedMenuForItemPath = file.file.absolutePath },
                                    onDismissMenu = { expandedMenuForItemPath = null },
                                    onUnhideClick = {
                                        viewModel.requestUnhideItem(file)
                                        expandedMenuForItemPath = null
                                    },
                                    onDeleteClick = {
                                        viewModel.requestDeleteItem(file)
                                        expandedMenuForItemPath = null
                                    },
                                    onRenameClick = {
                                        itemToRename = file
                                        showRenameDialog = true
                                        expandedMenuForItemPath = null
                                    },
                                    onClick = {
                                        val index = mediaFiles.indexOf(file)
                                        navController.navigate("media_viewer/${mediaType.name}/$index")
                                    },
                                    isGridView = isGridView,
                                    onShareClick = {}
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
}
