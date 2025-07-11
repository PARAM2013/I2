package com.example.secure.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.ui.dashboard.MainDashboardViewModel
import java.net.URLEncoder
import kotlin.text.Charsets
import com.example.secure.MainActivity.NavRoutes // For navigation routes

enum class GalleryFilter {
    ALL, IMAGES, VIDEOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: MainDashboardViewModel = viewModel() // Using MainDashboardViewModel for now
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var currentFilter by remember { mutableStateOf(GalleryFilter.ALL) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val allMediaFiles = remember(uiState.vaultStats) {
        uiState.vaultStats?.allFiles
            ?.filter { it.category == FileManager.FileCategory.PHOTO || it.category == FileManager.FileCategory.VIDEO }
            ?.sortedByDescending { it.file.lastModified() } // Sort by date, newest first
            ?: emptyList()
    }

    val filteredMedia = remember(currentFilter, allMediaFiles) {
        when (currentFilter) {
            GalleryFilter.IMAGES -> allMediaFiles.filter { it.category == FileManager.FileCategory.PHOTO }
            GalleryFilter.VIDEOS -> allMediaFiles.filter { it.category == FileManager.FileCategory.VIDEO }
            GalleryFilter.ALL -> allMediaFiles
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_gallery)) }, // TODO: Add string R.string.title_gallery
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Filled.FilterList, contentDescription = stringResource(R.string.gallery_filter_icon_desc)) // TODO: Add string R.string.gallery_filter_icon_desc
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.gallery_filter_all)) }, // TODO: Add string R.string.gallery_filter_all
                                onClick = { currentFilter = GalleryFilter.ALL; showFilterMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.gallery_filter_images)) }, // TODO: Add string R.string.gallery_filter_images
                                onClick = { currentFilter = GalleryFilter.IMAGES; showFilterMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.gallery_filter_videos)) }, // TODO: Add string R.string.gallery_filter_videos
                                onClick = { currentFilter = GalleryFilter.VIDEOS; showFilterMenu = false }
                            )
                        }
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
            if (uiState.isLoading && allMediaFiles.isEmpty()) { // Show loader only if initial load
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (filteredMedia.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = when(currentFilter) {
                            GalleryFilter.IMAGES -> Icons.Filled.Image
                            GalleryFilter.VIDEOS -> Icons.Filled.Videocam
                            GalleryFilter.ALL -> Icons.Filled.BrokenImage // Or a more generic "empty gallery" icon
                        },
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when(currentFilter) {
                            GalleryFilter.IMAGES -> stringResource(R.string.gallery_empty_images) // TODO: Add string
                            GalleryFilter.VIDEOS -> stringResource(R.string.gallery_empty_videos) // TODO: Add string
                            GalleryFilter.ALL -> stringResource(R.string.gallery_empty_all) // TODO: Add string
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(filteredMedia, key = { _, item -> item.file.absolutePath }) { index, mediaFile ->
                        GalleryItem(
                            mediaFile = mediaFile,
                            onClick = {
                                val mediaUris = filteredMedia.map { Uri.fromFile(it.file).toString() }
                                val encodedUris = mediaUris.joinToString(",") { URLEncoder.encode(it, Charsets.UTF_8.name()) }
                                val route = if (mediaFile.category == FileManager.FileCategory.PHOTO) {
                                    NavRoutes.IMAGE_VIEWER
                                } else {
                                    NavRoutes.VIDEO_PLAYER
                                }
                                navController.navigate("${route}/$index?${NavRoutes.ARG_MEDIA_URIS}=${encodedUris}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(
    mediaFile: FileManager.VaultFile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f) // Square items
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = mediaFile.file,
                contentDescription = mediaFile.file.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(
                    id = if (mediaFile.category == FileManager.FileCategory.PHOTO) R.drawable.ic_placeholder_image else R.drawable.ic_placeholder_video
                ),
                error = painterResource(
                    id = if (mediaFile.category == FileManager.FileCategory.PHOTO) R.drawable.ic_error_image else R.drawable.ic_error_video
                )
            )
            if (mediaFile.category == FileManager.FileCategory.VIDEO) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = stringResource(R.string.video_indicator_desc), // TODO: Add string
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer // Or a contrasting color
                )
            }
        }
    }
}
