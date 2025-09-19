package com.example.secure.ui.viewer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

/**
 * Finds the Activity from a Context
 */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Toggles fullscreen mode with landscape orientation
 */
fun toggleFullscreen(context: Context, isFullscreen: Boolean) {
    val activity = context.findActivity() ?: return
    
    if (isFullscreen) {
        // Enter fullscreen
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Hide system bars
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // Keep screen on
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        // Exit fullscreen
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        // Show system bars
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).show(WindowInsetsCompat.Type.systemBars())
        
        // Allow screen to turn off
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}