package com.example.secure.ui.viewer

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun VideoPlayer(
    file: File,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val player = remember { PlayerManager.getPlayer(context) }
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalContext.current as Activity
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var showControls by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableStateOf(1f) }
    var isLooping by remember { mutableStateOf(false) }

    var videoPosition by remember { mutableStateOf(0L) }
    var videoDuration by remember { mutableStateOf(0L) }

    val configuration = LocalConfiguration.current

    var brightness by remember { mutableStateOf(activity.window.attributes.screenBrightness) }
    var volume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) }

    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var showVolumeIndicator by remember { mutableStateOf(false) }

    val window = activity.window
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    LaunchedEffect(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(file) {
        PlayerManager.play(file)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> PlayerManager.pausePlayer()
                Lifecycle.Event.ON_RESUME -> PlayerManager.resumePlayer()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoPosition = player.currentPosition
            videoDuration = player.duration
            delay(1000)
        }
    }

    LaunchedEffect(showControls, showBrightnessIndicator, showVolumeIndicator) {
        if (showControls || showBrightnessIndicator || showVolumeIndicator) {
            delay(3000)
            showControls = false
            showBrightnessIndicator = false
            showVolumeIndicator = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        if (offset.x < size.width / 2) {
                            PlayerManager.seekBackward()
                        } else {
                            PlayerManager.seekForward()
                        }
                    },
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val screenWidth = size.width
                        if (change.position.x < screenWidth / 2) {
                            // Left side: Brightness
                            val newBrightness = (brightness - dragAmount / size.height).coerceIn(0f, 1f)
                            brightness = newBrightness
                            activity.window.attributes = activity.window.attributes.apply {
                                screenBrightness = newBrightness
                            }
                            showBrightnessIndicator = true
                            showVolumeIndicator = false
                        } else {
                            // Right side: Volume
                            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val newVolume = (currentVolume - (dragAmount / size.height * maxVolume)).toInt().coerceIn(0, maxVolume)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                            volume = newVolume.toFloat() / maxVolume
                            showVolumeIndicator = true
                            showBrightnessIndicator = false
                        }
                    },
                    onDragEnd = {
                        showBrightnessIndicator = false
                        showVolumeIndicator = false
                    }
                )
            }
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

        VideoPlayerControls(
            isVisible = showControls,
            isPlaying = isPlaying,
            videoPosition = videoPosition,
            videoDuration = videoDuration,
            onPlayPauseClicked = {
                if (isPlaying) PlayerManager.pausePlayer() else PlayerManager.resumePlayer()
            },
            onSeekForward = { PlayerManager.seekForward() },
            onSeekBackward = { PlayerManager.seekBackward() },
            onSeek = { position -> player.seekTo(position) },
            onPlaybackSpeedChange = { speed ->
                playbackSpeed = speed
                PlayerManager.setPlaybackSpeed(speed)
            },
            onLoopToggle = {
                isLooping = !isLooping
                PlayerManager.toggleLoopMode()
            },
            onFullScreenToggle = {
                activity.requestedOrientation = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            },
            onClose = onClose,
            fileName = file.name,
            isLooping = isLooping,
            isFullScreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        )

        AnimatedVisibility(visible = showBrightnessIndicator, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.CenterStart).padding(16.dp)) {
            VerticalIndicator(level = brightness, icon = Icons.Default.Brightness7)
        }

        AnimatedVisibility(visible = showVolumeIndicator, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.CenterEnd).padding(16.dp)) {
            VerticalIndicator(level = volume, icon = Icons.AutoMirrored.Filled.VolumeUp)
        }
    }
}

@Composable
fun VerticalIndicator(level: Float, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(100.dp)
                .background(Color.Gray, shape = RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(level)
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
fun VideoPlayerControls(
    isVisible: Boolean,
    isPlaying: Boolean,
    videoPosition: Long,
    videoDuration: Long,
    onPlayPauseClicked: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onLoopToggle: () -> Unit,
    onFullScreenToggle: () -> Unit,
    onClose: () -> Unit,
    fileName: String,
    isLooping: Boolean,
    isFullScreen: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = fileName, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }

            // Center Controls
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSeekBackward) {
                    Icon(Icons.Default.Replay10, contentDescription = "Rewind 10 seconds", tint = Color.White, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.width(48.dp))
                IconButton(onClick = onPlayPauseClicked) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.width(48.dp))
                IconButton(onClick = onSeekForward) {
                    Icon(Icons.Default.Forward10, contentDescription = "Forward 10 seconds", tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }

            // Bottom Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDuration(videoPosition),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = videoPosition.toFloat(),
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..(videoDuration.toFloat().takeIf { it > 0 } ?: 0f),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDuration(videoDuration),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var showMoreMenu by remember { mutableStateOf(false) }
                    var showSpeedMenu by remember { mutableStateOf(false) }

                    IconButton(onClick = onFullScreenToggle) {
                        Icon(
                            if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Playback Speed") },
                                onClick = { showSpeedMenu = true }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isLooping) "Looping" else "Not Looping") },
                                onClick = {
                                    onLoopToggle()
                                    showMoreMenu = false
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false }
                        ) {
                            listOf(0.5f, 1f, 1.5f, 2f).forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${speed}x") },
                                    onClick = {
                                        onPlaybackSpeedChange(speed)
                                        showSpeedMenu = false
                                        showMoreMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
