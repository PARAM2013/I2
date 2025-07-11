package com.example.secure

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity // Required for BiometricPrompt
import com.example.secure.auth.PinManager
import com.example.secure.ui.composables.PinScreen
import com.example.secure.ui.theme.ISecureTheme

class LauncherActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the initial lock state. If a PIN exists, the app should be locked.
        // If no PIN, it will go to setup, which is a form of "locked" access.
        AppGlobalState.isLocked = PinManager.isPinSet(this)

        setContent {
            ISecureTheme {
                DetermineNextScreen()
            }
        }
    }

    @Composable
    private fun DetermineNextScreen() {
        // PinScreen will handle both setup and unlock scenarios based on PinManager.isPinSet()
        // and its internal ViewModel logic.
        // AppGlobalState.isLocked helps determine if MainActivity can be shown directly.
        // LauncherActivity's role is primarily initial authentication.

        if (AppGlobalState.isLocked || !PinManager.isPinSet(this)) {
            ShowPinScreen()
        } else {
            // This case implies PIN is set AND AppGlobalState.isLocked is false.
            // This could happen if app was already unlocked and LauncherActivity is somehow revisited
            // without AppGlobalState.isLocked being reset to true.
            // Generally, after unlock, MainActivity is shown and LauncherActivity is finished.
            navigateToMainApp()
        }
    }

    @Composable
    private fun ShowPinScreen() {
        PinScreen(
            activity = this@LauncherActivity,
            onNavigateToMain = {
                AppGlobalState.isLocked = false // Explicitly unlock
                navigateToMainApp()
            }
        )
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish LauncherActivity as its job is done
    }

    override fun onResume() {
        super.onResume()
        // When LauncherActivity resumes, it should re-evaluate the lock status.
        // If a PIN is set, the app should be considered locked until verified.
        if (PinManager.isPinSet(this)) {
            AppGlobalState.isLocked = true
        }
        // Re-trigger composition.
        setContent {
            ISecureTheme {
                DetermineNextScreen()
            }
        }
    }
}
