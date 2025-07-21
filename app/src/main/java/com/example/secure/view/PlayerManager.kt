package com.example.secure.view

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object PlayerManager {
    private var player: ExoPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                })
            }
        }
        return player!!
    }

    fun play(file: File) {
        val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    fun onResume() {
        player?.play()
    }

    fun onPause() {
        player?.pause()
    }

    fun releasePlayer() {
        player?.release()
        player = null
        _isPlaying.value = false
    }

    fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
    }
}
