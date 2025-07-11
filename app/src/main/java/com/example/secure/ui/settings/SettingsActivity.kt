package com.example.secure.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.secure.ui.pin.PinSetupActivity // Assuming this path, will adjust if different
import com.example.secure.ui.theme.ISecureTheme

class SettingsActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(application)
    }

    companion object {
        // Define a key for the mode if PinSetupActivity needs to distinguish
        // between initial setup and changing PIN.
        // This would ideally be in PinSetupActivity.kt's companion object.
        const val EXTRA_PIN_SETUP_MODE = "com.example.secure.PIN_SETUP_MODE"
        const val MODE_CHANGE_PIN = "change_pin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Removed XML layout and Fragment transaction
        // supportActionBar?.setDisplayHomeAsUpEnabled(true) // Will be handled by Compose TopAppBar

        setContent {
            ISecureTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToPinSetup = {
                            val intent = Intent(this, PinSetupActivity::class.java).apply {
                                putExtra(EXTRA_PIN_SETUP_MODE, MODE_CHANGE_PIN)
                            }
                            startActivity(intent)
                        },
                        onNavigateBack = {
                            finish() // Or onBackPressedDispatcher.onBackPressed()
                        }
                    )
                }
            }
        }
    }

    // onSupportNavigateUp is no longer strictly needed if Compose TopAppBar handles back.
    // If kept for ActionBar compatibility (e.g. if setDisplayHomeAsUpEnabled was still used),
    // it should call finish() or super.onSupportNavigateUp().
    // For a full Compose activity, this can often be removed.
    // override fun onSupportNavigateUp(): Boolean {
    //     finish()
    //     return true
    // }
}
