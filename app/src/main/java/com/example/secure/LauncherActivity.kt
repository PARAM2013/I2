package com.example.secure

import android.content.Intent
import android.os.Bundle
import com.example.secure.auth.PinManager
import com.example.secure.ui.lockscreen.LockScreenActivity
import com.example.secure.ui.pin.PinSetupActivity

class LauncherActivity : TrackedActivity() { // Extend TrackedActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Theme Theme.App.Starting is applied via Manifest, handles splash.
        // installSplashScreen() // Call this if not using a router activity theme like Theme.SplashScreen from manifest

        if (PinManager.isPinSet(this)) {
            AppGlobalState.isLocked = true // Ensure app is marked as locked
            val intent = Intent(this, LockScreenActivity::class.java)
            startActivity(intent)
        } else {
            // For PIN setup, isLocked can remain false or be explicitly set.
            // PinSetupActivity will set it to false upon successful PIN creation.
            AppGlobalState.isLocked = false // Or true, then PinSetupActivity unlocks it. Let's make it false.
            val intent = Intent(this, PinSetupActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}