package com.example.secure.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

@Composable
fun VideoPlayer(
    file: File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember { PlayerManager.getPlayer(context) }
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var showControls by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    var videoPosition by remember { mutableStateOf(0L) }
    var videoDuration by remember { mutableStateOf(0L) }

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
            PlayerManager.releasePlayer()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoPosition = player.currentPosition
            videoDuration = player.duration
            isMuted = PlayerManager.isMuted()
            delay(1000)
        }
    }

    // Auto-hide controls after 3 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        autoHideJob?.cancel()
        if (showControls && isPlaying) {
            autoHideJob = coroutineScope.launch {
                delay(3000)
                showControls = false
            }
        }
    }

    // Helper functions for skip operations
    fun skipBackward() {
        val newPosition = max(0L, videoPosition - 10000)
        player.seekTo(newPosition)
    }

    fun skipForward() {
        val newPosition = min(videoDuration, videoPosition + 10000)
        player.seekTo(newPosition)
    }

    fun toggleControlsVisibility() {
        showControls = !showControls
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { toggleControlsVisibility() },
                    onDoubleTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 2) {
                            skipBackward()
                        } else {
                            skipForward()
                        }
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

        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                // Time display
                Text(
                    text = "${formatTime(videoPosition)} / ${formatTime(videoDuration)}",
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Slider(
                    value = videoPosition.toFloat(),
                    onValueChange = { 
                        player.seekTo(it.toLong())
                        // Reset auto-hide timer on interaction
                        showControls = true
                    },
                    valueRange = 0f..videoDuration.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip backward button
                    IconButton(onClick = { 
                        skipBackward()
                        showControls = true
                    }) {
                        Icon(
                            Icons.Default.Replay10,
                            "Skip back 10 seconds",
                            tint = Color.White
                        )
                    }
                    
                    // Play/Pause button
                    IconButton(onClick = { 
                        if (isPlaying) player.pause() else player.play()
                        showControls = true
                    }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                    
                    // Skip forward button
                    IconButton(onClick = { 
                        skipForward()
                        showControls = true
                    }) {
                        Icon(
                            Icons.Default.Forward10,
                            "Skip forward 10 seconds",
                            tint = Color.White
                        )
                    }
                    
                    // Mute/Unmute button
                    IconButton(onClick = { 
                        PlayerManager.toggleMute()
                        isMuted = PlayerManager.isMuted()
                        showControls = true
                    }) {
                        Icon(
                            if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            if (isMuted) "Unmute" else "Mute",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
