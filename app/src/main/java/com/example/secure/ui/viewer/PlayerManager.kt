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

    fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        return exoPlayer!!
    }

    fun setupPlayerView(playerView: PlayerView, file: File, showControls: Boolean) {
        val player = getOrCreatePlayer()
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
        player.prepare()
        
        playerView.player = player
        playerView.useController = true
        playerView.controllerShowTimeoutMs = if (showControls) 3000 else 0
        playerView.controllerHideOnTouch = true
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
