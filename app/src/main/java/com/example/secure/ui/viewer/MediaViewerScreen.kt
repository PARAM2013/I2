package com.example.secure.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animate
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewerScreen(
    files: List<File>,
    initialIndex: Int,
    onClose: () -> Unit,
    onDelete: (File) -> Unit,
    onUnhide: (File) -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var currentFile by remember { mutableStateOf(files[initialIndex]) }
    val pagerState = rememberPagerState(initialPage = initialIndex) { files.size }
    val scope = rememberCoroutineScope()
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(pagerState.currentPage) {
        currentFile = files[pagerState.currentPage]
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
                detectVerticalDragGestures(
                    onDragStart = { offsetY = 0f; scale = 1f },
                    onDragEnd = {
                        if (offsetY.absoluteValue > 200) {
                            onClose()
                        } else {
                            scope.launch {
                                animate(offsetY, 0f) { value, _ -> offsetY = value }
                                animate(scale, 1f) { value, _ -> scale = value }
                            }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        offsetY += dragAmount
                        scale = 1f - (offsetY.absoluteValue / 1000f).coerceIn(0f, 0.5f)
                    }
                )
            }
            .graphicsLayer {
                translationY = offsetY
                scaleX = scale
                scaleY = scale
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { files[it].absolutePath } // Add key for state management
        ) { page ->
            val file = files[page]
            when {
                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif") -> {
                    ZoomableImage(
                        file = file,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                file.extension.lowercase() in listOf("mp4", "mkv", "webm", "avi", "3gp") -> {
                    VideoPlayer(
                        file = file,
                        showControls = showControls,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        // Navigation Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = { 
                            scope.launch { 
                                pagerState.animateScrollToPage(pagerState.currentPage - 1) 
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, "Previous", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
                
                if (pagerState.currentPage < files.size - 1) {
                    IconButton(
                        onClick = { 
                            scope.launch { 
                                pagerState.animateScrollToPage(pagerState.currentPage + 1) 
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, "Next", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }

        // Top Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                    }
                    Text(
                        text = currentFile.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row {
                        IconButton(onClick = { onUnhide(currentFile) }) {
                            Icon(Icons.Filled.Visibility, "Unhide", tint = Color.White)
                        }
                        IconButton(onClick = { onDelete(currentFile) }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Bottom Info
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    Text(
                        "Modified: ${dateFormat.format(Date(currentFile.lastModified()))}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Size: ${formatFileSize(currentFile.length())}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    file: File,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Box(modifier = modifier) {
        AsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .transformable(
                    state = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale = (scale * zoomChange).coerceIn(0.5f..5f)
                        offset += offsetChange
                    }
                )
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun VideoPlayer(
    file: File,
    showControls: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerManager = remember { PlayerManager(context) }
    var showSpeedControls by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(playerManager.getPlaybackSpeed()) }

    DisposableEffect(Unit) {
        onDispose {
            playerManager.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).also { playerView ->
                    playerManager.setupPlayerView(playerView, file, showControls)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Playback Speed Controls
        AnimatedVisibility(
            visible = showControls && showSpeedControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                Text(
                    "Playback Speed",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                        TextButton(
                            onClick = {
                                playbackSpeed = speed
                                playerManager.setPlaybackSpeed(speed)
                                showSpeedControls = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (playbackSpeed == speed) MaterialTheme.colorScheme.primary else Color.White
                            )
                        ) {
                            Text("${speed}x")
                        }
                    }
                }
            }
        }
        
        // Video Controls
        AnimatedVisibility(
            visible = showControls && !showSpeedControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Previous video functionality handled by parent */ }) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White)
                }
                
                IconButton(onClick = { 
                    playerManager.getOrCreatePlayer().let { player ->
                        if (player.isPlaying) player.pause() else player.play()
                    }
                }) {
                    Icon(
                        if (playerManager.getOrCreatePlayer().isPlaying) 
                            Icons.Default.Pause 
                        else 
                            Icons.Default.PlayArrow,
                        "Play/Pause",
                        tint = Color.White
                    )
                }
                
                IconButton(onClick = { /* Next video functionality handled by parent */ }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White)
                }
                
                TextButton(onClick = { showSpeedControls = true }) {
                    Text("${playbackSpeed}x", color = Color.White)
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var fileSize = size.toFloat()
    var unitIndex = 0
    while (fileSize > 1024 && unitIndex < units.size - 1) {
        fileSize /= 1024
        unitIndex++
    }
    return String.format("%.1f %s", fileSize, units[unitIndex])
}
