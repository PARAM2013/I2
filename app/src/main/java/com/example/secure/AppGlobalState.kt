package com.example.secure

import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import java.util.TimerTask
import android.os.Handler
import android.os.Looper

object AppGlobalState {
    var isLocked = true // Start as locked, LauncherActivity will decide
    var currentActivity: AppCompatActivity? = null // Track current foreground activity

    private var inactivityTimer: Timer? = null
    private const val INACTIVITY_TIMEOUT_MS = 90000L // 90 seconds
    private val handler = Handler(Looper.getMainLooper())

    fun onActivityResumed(activity: AppCompatActivity) {
        currentActivity = activity
        // Add LauncherActivity to the list of activities that don't get auto-redirected to LockScreen
        if (isLocked && activity !is com.example.secure.ui.lockscreen.LockScreenActivity &&
            activity !is com.example.secure.ui.pin.PinSetupActivity &&
            activity !is com.example.secure.LauncherActivity) {
            // If the app is supposed to be locked and we are not already on a lock/setup screen, redirect.
            val intent = android.content.Intent(activity, com.example.secure.ui.lockscreen.LockScreenActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
            return // Important: stop further execution in the original activity's onResume
        }
        resetInactivityTimer()
    }

    fun onActivityPaused(activity: AppCompatActivity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
        stopInactivityTimer()
        // Optional: Consider if app should lock immediately when any sensitive activity is paused.
        // For now, rely on timeout or LauncherActivity re-check.
        // If MainActivity is paused, we can assume it might go to background.
        // If it's not LockScreenActivity or PinSetupActivity that is being paused.
        if (activity is MainActivity) {
             // If app goes to background from MainActivity, set it to locked after timeout.
             // The timer is already running, if it fires, it will set isLocked = true.
             // Or, more aggressively, set isLocked = true after a shorter delay or immediately.
             // For now, the existing timer will handle this.
        }
    }

    fun onUserInteraction() {
        resetInactivityTimer()
    }

    private fun resetInactivityTimer() {
        stopInactivityTimer()
        if (currentActivity is MainActivity) { // Only run timer if a main app activity is active
            inactivityTimer = Timer()
            inactivityTimer?.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        if (currentActivity is MainActivity) { // Check again, activity might have changed
                            isLocked = true
                            // No need to navigate from here, onResume of next activity will handle it,
                            // or LauncherActivity if app is restarted.
                            // This ensures if MainActivity is in foreground and timeout occurs,
                            // next interaction or resume will trigger lock screen.
                            android.util.Log.d("AppGlobalState", "Inactivity timeout. App is now locked.")
                        }
                    }
                }
            }, INACTIVITY_TIMEOUT_MS)
        }
    }

    private fun stopInactivityTimer() {
        inactivityTimer?.cancel()
        inactivityTimer = null
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
