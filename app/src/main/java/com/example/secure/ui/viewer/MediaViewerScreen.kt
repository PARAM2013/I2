package com.example.secure.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.secure.ui.theme.ISecureTheme
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, androidx.media3.common.util.UnstableApi::class)
@Composable
fun MediaViewerScreen(
    files: List<File>,
    initialIndex: Int,
    onClose: () -> Unit,
    onDelete: (File) -> Unit,
    onUnhide: (File) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { files.size })
    var showControls by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val playerManager = remember {
        PlayerManager(context) {
            scope.launch {
                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            playerManager.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures(onTap = { showControls = !showControls }) }
    ) {
        HorizontalPager(
            state = pagerState,
            key = { files[it].absolutePath },
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val file = files[page]
            when {
                file.isImage() -> ZoomableImage(file = file, modifier = Modifier.fillMaxSize())
                file.isVideo() -> VideoPlayer(
                    file = file,
                    playerManager = playerManager,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
            MediaControls(
                file = files[pagerState.currentPage],
                onClose = onClose,
                onDelete = { onDelete(files[pagerState.currentPage]) },
                onUnhide = { onUnhide(files[pagerState.currentPage]) },
                isFirst = pagerState.currentPage == 0,
                isLast = pagerState.currentPage == pagerState.pageCount - 1,
                onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                isVideo = files[pagerState.currentPage].isVideo(),
                playerManager = playerManager
            )
        }
    }
}

@Composable
private fun MediaControls(
    file: File,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onUnhide: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isVideo: Boolean,
    playerManager: PlayerManager
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close", tint = Color.White)
            }
            Text(file.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Row {
                IconButton(onClick = onUnhide) { Icon(Icons.Filled.Visibility, "Unhide", tint = Color.White) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Delete", tint = Color.White) }
            }
        }

        // Center Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!isFirst) {
                IconButton(onClick = onPrevious) { Icon(Icons.Default.NavigateBefore, "Previous", tint = Color.White, modifier = Modifier.size(48.dp)) }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            if (!isLast) {
                IconButton(onClick = onNext) { Icon(Icons.Default.NavigateNext, "Next", tint = Color.White, modifier = Modifier.size(48.dp)) }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Bottom Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                .padding(16.dp)
        ) {
            if (isVideo) {
                VideoPlaybackControls(playerManager)
            }
            Text("Modified: ${file.lastModified().formatDate()}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text("Size: ${file.length().formatSize()}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun VideoPlaybackControls(playerManager: PlayerManager) {
    var isPlaying by remember { mutableStateOf(playerManager.isPlaying()) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    val duration by remember { mutableLongStateOf(0L) }
    var showSpeedControls by remember { mutableStateOf(false) }

    LaunchedEffect(playerManager) {
        while (true) {
            isPlaying = playerManager.isPlaying()
            currentPosition = playerManager.getCurrentPosition()
            delay(1000)
        }
    }

    // Seeker
    Slider(
        value = currentPosition.toFloat(),
        onValueChange = { playerManager.seekTo(it.toLong()) },
        valueRange = 0f..(duration.toFloat().takeIf { it > 0 } ?: 0f),
        modifier = Modifier.fillMaxWidth()
    )

    // Controls
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { if (isPlaying) playerManager.pause() else playerManager.play() }) {
            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = Color.White, modifier = Modifier.size(48.dp))
        }
        TextButton(onClick = { showSpeedControls = !showSpeedControls }) {
            Text("1.0x", color = Color.White) // Placeholder for current speed
        }
    }

    // Speed Controls
    AnimatedVisibility(visible = showSpeedControls) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(0.5f, 1f, 1.5f, 2f).forEach { speed ->
                TextButton(onClick = { playerManager.setPlaybackSpeed(speed); showSpeedControls = false }) {
                    Text("${speed}x", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(file: File, modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = {
                    scale = if (scale > 1f) 1f else 2f
                    offset = Offset.Zero
                })
            }
            .transformable(
                state = rememberTransformableState { zoomChange, panChange, _ ->
                    scale = (scale * zoomChange).coerceIn(0.5f, 5f)
                    offset += panChange
                }
            )
    ) {
        AsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
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

@UnstableApi
@Composable
private fun VideoPlayer(file: File, playerManager: PlayerManager, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).also { playerView ->
                playerManager.setupPlayerView(playerView, file)
            }
        },
        modifier = modifier
    )
}

// Extension functions for better readability
private fun File.isImage(): Boolean = extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
private fun File.isVideo(): Boolean = extension.lowercase() in listOf("mp4", "mkv", "webm", "avi", "3gp")
private fun Long.formatDate(): String = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(this))
private fun Long.formatSize(): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = toFloat()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    return String.format("%.1f %s", size, units[unitIndex])
}
