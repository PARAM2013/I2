package com.example.secure.ui.viewer

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object PlayerManager {
    private var player: ExoPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private var previousVolume: Float = 1f
    private var isMuted: Boolean = false

    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
            player?.addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
            })
        }
        return player!!
    }

    fun play(file: File) {
        val mediaItem = MediaItem.fromUri(file.absolutePath)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    fun pausePlayer() {
        player?.pause()
    }

    fun resumePlayer() {
        player?.play()
    }

    fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    fun mutePlayer() {
        player?.let { player ->
            if (!isMuted) {
                previousVolume = player.volume
                player.volume = 0f
                isMuted = true
            }
        }
    }

    fun unmutePlayer() {
        player?.let { player ->
            if (isMuted) {
                player.volume = previousVolume
                isMuted = false
            }
        }
    }

    fun toggleMute() {
        if (isMuted) {
            unmutePlayer()
        } else {
            mutePlayer()
        }
    }

    fun isMuted(): Boolean = isMuted

    fun getCurrentVolume(): Float = player?.volume ?: 1f

    fun setVolume(volume: Float) {
        player?.volume = volume.coerceIn(0f, 1f)
        if (!isMuted) {
            previousVolume = volume
        }
    }

    fun releasePlayer() {
        player?.release()
        player = null
        isMuted = false
        previousVolume = 1f
    }
}
