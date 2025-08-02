package com.example.secure.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewerScreen(
    files: List<File>,
    initialIndex: Int,
    onClose: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var currentFile by remember { mutableStateOf(files[initialIndex]) }
    val pagerState = rememberPagerState(initialPage = initialIndex) { files.size }
    var currentImageScale by remember { mutableStateOf(1f) }

    LaunchedEffect(pagerState.currentPage) {
        currentFile = files[pagerState.currentPage]
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { files[it].absolutePath } // Add key for state management
        ) { page ->
            val file = files[page]
            // currentImageScale is now declared at a higher scope

            when {
                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif") -> {
                    ZoomableImage(
                        file = file,
                        modifier = Modifier.fillMaxSize(),
                        pagerState = pagerState,
                        onScaleChange = { scale -> currentImageScale = scale },
                        onSingleTap = { showControls = !showControls }
                    )
                }
                file.extension.lowercase() in listOf("mp4", "mkv", "webm", "avi", "3gp") -> {
                    VideoPlayer(
                        file = file,
                        pagerState = pagerState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // Handle other file types or show a placeholder
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close viewer", tint = Color.White)
                    }
                    Text(
                        text = currentFile.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
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

fun formatFileSize(size: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var fileSize = size.toFloat()
    var unitIndex = 0
    while (fileSize > 1024 && unitIndex < units.size - 1) {
        fileSize /= 1024
        unitIndex++
    }
    return String.format("%.1f %s", fileSize, units[unitIndex])
}