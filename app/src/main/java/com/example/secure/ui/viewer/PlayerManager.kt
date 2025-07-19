package com.example.secure.ui.viewer

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

@UnstableApi
class PlayerManager(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null
    private var currentPlaybackSpeed: Float = 1.0f

    fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
            }
        }
        return exoPlayer!!
    }

    fun setupPlayerView(playerView: PlayerView, file: File, showControls: Boolean) {
        val player = getOrCreatePlayer()
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
        player.prepare()
        player.playbackParameters = androidx.media3.common.PlaybackParameters(currentPlaybackSpeed)
        
        playerView.player = player
        playerView.useController = true
        playerView.controllerShowTimeoutMs = if (showControls) 3000 else 0
        playerView.controllerHideOnTouch = true
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        currentPlaybackSpeed = speed
        exoPlayer?.let { player ->
            player.playbackParameters = androidx.media3.common.PlaybackParameters(speed)
        }
    }
    
    fun getPlaybackSpeed(): Float {
        return currentPlaybackSpeed
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
