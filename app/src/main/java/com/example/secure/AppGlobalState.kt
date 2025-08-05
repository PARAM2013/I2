package com.example.secure

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.TimerTask
import android.os.Handler
import android.os.Looper
import android.util.Log

object AppGlobalState {
    var isLocked = true // Start as locked, LauncherActivity will decide
    private var currentActivityRef: WeakReference<Activity>? = null
    val currentActivity: Activity?
        get() = currentActivityRef?.get()

    private var inactivityTimer: Timer? = null
    private const val INACTIVITY_TIMEOUT_MS = 15000L // 15 seconds
    private val handler = Handler(Looper.getMainLooper())

    fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        // If app is locked and MainActivity attempts to resume, redirect to Launcher to show PinScreen
        if (isLocked && activity is MainActivity) {
            val intent = Intent(activity, LauncherActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
            return
        }
        resetInactivityTimer()
    }

    fun onActivityPaused(activity: Activity) {
        // When an activity is paused, we start or restart the inactivity timer.
        // The timer should only run if a data-sensitive activity (like MainActivity) is active.
        if (currentActivity is MainActivity) {
             resetInactivityTimer() // Start timer when MainActivity is paused (goes to background)
        } else {
            stopInactivityTimer() // Stop timer if PinScreen or other non-sensitive screen is active
        }
    }

    fun onUserInteraction() {
        // Only reset timer if user is interacting with MainActivity
        if (currentActivity is MainActivity) {
            resetInactivityTimer()
        }
    }

    private fun resetInactivityTimer() {
        stopInactivityTimer()
        // Only schedule timer if MainActivity is the current one and the app is not locked.
        if (currentActivity is MainActivity && !isLocked) {
            inactivityTimer = Timer()
            inactivityTimer?.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        // Check again: if still in MainActivity and app is not already locked
                        if (currentActivity is MainActivity && !isLocked) {
                            isLocked = true
                            Log.d("AppGlobalState", "Inactivity timeout. App is now locked.")
                        }
                    }
                }
            }, INACTIVITY_TIMEOUT_MS)
        }
    }

    private fun stopInactivityTimer() {
        inactivityTimer?.cancel()
        inactivityTimer = null
        Log.d("AppGlobalState", "Inactivity timer stopped.")
    }
}

// BaseActivity to automatically register with AppGlobalState
abstract class TrackedActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        AppGlobalState.onActivityResumed(this)
    }

    override fun onPause() {
        super.onPause()
        AppGlobalState.onActivityPaused(this)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        AppGlobalState.onUserInteraction()
    }
}
