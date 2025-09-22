package com.example.secure.ui.viewer

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.min

import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun VideoPlayer(
    file: File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showControls by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isLooping by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    var videoPosition by remember { mutableStateOf(0L) }
    var videoDuration by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(true) } // Assume playing by default

    // Initialize ExoPlayer
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(currentIsPlaying: Boolean) {
                    isPlaying = currentIsPlaying
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        // Optionally loop or pause at the end
                    }
                }
            })
        }
    }

    // Set media item and prepare player when file changes
    LaunchedEffect(file) {
        val mediaItem = MediaItem.fromUri(file.absolutePath)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    // Update video position and duration
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoPosition = player.currentPosition
            videoDuration = player.duration
            isMuted = player.volume == 0f // Update muted state from player
            isLooping = player.repeatMode == Player.REPEAT_MODE_ONE // Update looping state from player
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

    // Release player when Composable is disposed
    DisposableEffect(player) {
        onDispose {
            player.release()
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
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                val (timeDisplay, slider, row1, row2) = createRefs()

                // Time display
                Text(
                    text = "${formatTime(videoPosition)} / ${formatTime(videoDuration)}",
                    color = Color.White,
                    modifier = Modifier.constrainAs(timeDisplay) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                    }.padding(bottom = 8.dp)
                )
                
                Slider(
                    value = videoPosition.toFloat(),
                    onValueChange = { 
                        player.seekTo(it.toLong())
                        // Reset auto-hide timer on interaction
                        showControls = true
                    },
                    valueRange = 0f..videoDuration.toFloat(),
                    modifier = Modifier.constrainAs(slider) {
                        top.linkTo(timeDisplay.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )
                
                // First row of controls
                Row(
                    modifier = Modifier.constrainAs(row1) {
                        top.linkTo(slider.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
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
                }
                
                Spacer(modifier = Modifier.height(8.dp).constrainAs(createRef()) {
                    top.linkTo(row1.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })
                
                // Second row of controls
                Row(
                    modifier = Modifier.constrainAs(row2) {
                        top.linkTo(row1.bottom, margin = 8.dp) // Link to row1.bottom with a margin
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute/Unmute button
                    IconButton(onClick = { 
                        player.volume = if (isMuted) 1f else 0f
                        isMuted = !isMuted
                        showControls = true
                    }) {
                        Icon(
                            if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            if (isMuted) "Unmute" else "Mute",
                            tint = Color.White
                        )
                    }
                    
                    // Loop toggle button
                    IconButton(onClick = { 
                        player.repeatMode = if (isLooping) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
                        isLooping = !isLooping
                        showControls = true
                    }) {
                        Icon(
                            if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                            if (isLooping) "Disable loop" else "Enable loop",
                            tint = if (isLooping) Color.Yellow else Color.White
                        )
                    }
                    
                    // Fullscreen toggle button
                    IconButton(onClick = { 
                        isFullscreen = !isFullscreen
                        toggleFullscreen(context, isFullscreen)
                        showControls = true
                    }) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}