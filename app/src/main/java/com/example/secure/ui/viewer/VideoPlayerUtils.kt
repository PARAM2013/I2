package com.example.secure.ui.viewer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlinx.coroutines.delay

/**
 * Formats time in milliseconds to MM:SS or HH:MM:SS format
 */
fun formatTime(timeMs: Long): String {
    if (timeMs < 0) return "00:00"
    
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Handles double-tap gestures for skip functionality
 */
suspend fun PointerInputScope.detectVideoGestures(
    onSingleTap: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit
) {
    detectTapGestures(
        onTap = { onSingleTap() },
        onDoubleTap = { offset ->
            val screenWidth = size.width
            if (offset.x < screenWidth / 2) {
                onDoubleTapLeft()
            } else {
                onDoubleTapRight()
            }
        }
    )
}