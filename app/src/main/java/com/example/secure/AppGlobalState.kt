package com.example.secure

import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import java.util.TimerTask
import android.os.Handler
import android.os.Looper

object AppGlobalState {
    var isLocked = true // Start as locked, LauncherActivity will decide
    var currentActivity: android.app.Activity? = null // Track current foreground activity, more generic type
    private var inactivityTimer: Timer? = null
    private const val INACTIVITY_TIMEOUT_MS = 15000L // 90 seconds
    private val handler = Handler(Looper.getMainLooper())

    fun onActivityResumed(activity: android.app.Activity) { // Use android.app.Activity
        currentActivity = activity
        // LauncherActivity now handles its own redirection to PinScreen.
        // This logic here was to force a lock screen if a regular activity resumed while app was locked.
        // PinScreen is now the gatekeeper. If MainActivity resumes and isLocked is true, it should redirect.
        // Let's simplify: if isLocked is true, and the current activity is NOT LauncherActivity (which shows PinScreen),
        // then it implies an activity behind the PinScreen somehow became active.
        // This check might be more suited for a BaseActivity for all activities *except* Launcher.
        if (isLocked && activity !is LauncherActivity && activity is MainActivity) {
            // If app is locked and MainActivity attempts to resume, redirect to Launcher to show PinScreen
            val intent = android.content.Intent(activity, LauncherActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
            return
        }
        resetInactivityTimer()
    }

    fun onActivityPaused(activity: android.app.Activity) { // Use android.app.Activity
        if (currentActivity == activity) {
            // currentActivity = null // Keep currentActivity to know what was last active for timer logic
        }
        // When an activity is paused, we start or restart the inactivity timer.
        // If the app goes to background, and currentActivity was MainActivity, the timer will eventually lock.
        // If currentActivity is LauncherActivity (showing PinScreen), we don't want to auto-lock based on this timer.
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
        // Only schedule timer if MainActivity is the current one (or anticipated to be).
        // This check ensures we don't run the timer when PinScreen is up.
        if (currentActivity is MainActivity && !isLocked) {
            inactivityTimer = Timer()
            inactivityTimer?.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        // Check again: if still in MainActivity and app is not already locked by other means
                        if (currentActivity is MainActivity && !isLocked) {
                            isLocked = true
                            android.util.Log.d("AppGlobalState", "Inactivity timeout. App is now locked. MainActivity was active.")
                            // Optional: force navigation to LockScreen if MainActivity is still in foreground
                            // This is aggressive. Usually, the lock is enforced upon next resume.
                            // currentActivity?.let {
                            //     if (it is MainActivity && !it.isFinishing) {
                            //         val intent = android.content.Intent(it, LauncherActivity::class.java)
                            //         intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                            //         it.startActivity(intent)
                            //         it.finish()
                            //     }
                            // }
                        }
                    }
                }
            }, INACTIVITY_TIMEOUT_MS)
        }
    }

    private fun stopInactivityTimer() {
        inactivityTimer?.cancel()
        inactivityTimer = null
        android.util.Log.d("AppGlobalState", "Inactivity timer stopped.")
    }
}

// BaseActivity to automatically register with AppGlobalState
abstract class TrackedActivity : AppCompatActivity() { // Keep AppCompatActivity if other features rely on it
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
