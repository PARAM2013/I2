package com.example.secure.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayer(
    file: File,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember { PlayerManager.getPlayer(context) }
    val isPlaying by PlayerManager.isPlaying.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    var wasPlayingBeforePause by remember { mutableStateOf(false) }

    var showControls by remember { mutableStateOf(false) }
    var showSettingsPopup by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var isFullscreen by remember { mutableStateOf(true) }

    var videoPosition by remember { mutableStateOf(0L) }
    var videoDuration by remember { mutableStateOf(0L) }

    LaunchedEffect(file) {
        PlayerManager.play(file)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (player.isPlaying) {
                        wasPlayingBeforePause = true
                        player.pause()
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (wasPlayingBeforePause) {
                        player.play()
                        wasPlayingBeforePause = false
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            PlayerManager.releasePlayer()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoPosition = player.currentPosition
            videoDuration = player.duration
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Slider(
                    value = videoPosition.toFloat(),
                    onValueChange = { player.seekTo(it.toLong()) },
                    valueRange = 0f..videoDuration.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val scope = rememberCoroutineScope()
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White)
                    }

                    IconButton(onClick = { player.seekTo(player.currentPosition - 10000) }) {
                        Icon(Icons.Default.FastRewind, "Rewind", tint = Color.White)
                    }

                    IconButton(onClick = { if (isPlaying) player.pause() else player.play() }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { player.seekTo(player.currentPosition + 10000) }) {
                        Icon(Icons.Default.FastForward, "Forward", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        enabled = pagerState.currentPage < pagerState.pageCount - 1
                    ) {
                        Icon(Icons.Default.SkipNext, "Next", tint = Color.White)
                    }

                    val view = LocalView.current
                    val window = (view.context as Activity).window
                    val insetsController = WindowCompat.getInsetsController(window, view)

                    IconButton(
                        onClick = {
                            isFullscreen = !isFullscreen
                            if (isFullscreen) {
                                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                            } else {
                                insetsController.show(WindowInsetsCompat.Type.systemBars())
                            }
                        }
                    ) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { showSettingsPopup = !showSettingsPopup }) {
                        Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                    }
                }

                if (showSettingsPopup) {
                    val view = LocalView.current
                    val window = (view.context as Activity).window

                    var volume by remember { mutableStateOf(player.volume) }
                    var brightness by remember { mutableStateOf(window.attributes.screenBrightness.takeIf { it > 0 } ?: 0.5f) }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Volume Control
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, "Volume", tint = Color.White)
                            Slider(
                                value = volume,
                                onValueChange = {
                                    volume = it
                                    player.volume = it
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Brightness Control
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BrightnessMedium, "Brightness", tint = Color.White)
                            Slider(
                                value = brightness,
                                onValueChange = {
                                    brightness = it
                                    val attributes = window.attributes
                                    attributes.screenBrightness = it
                                    window.attributes = attributes
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Speed Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(0.5f, 1f, 1.5f, 2f).forEach { speed ->
                                TextButton(
                                    onClick = {
                                        playbackSpeed = speed
                                        PlayerManager.setPlaybackSpeed(speed)
                                    }
                                ) {
                                    Text("${speed}x", color = if (speed == playbackSpeed) Color.Yellow else Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
