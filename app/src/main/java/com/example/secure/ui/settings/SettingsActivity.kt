package com.example.secure.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.secure.ui.theme.ISecureTheme // Corrected import

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Action bar setup
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings" // Set title for the ActionBar

        setContent {
            ISecureTheme { // Corrected theme name
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen(
                        activityContext = this
                    )
                }
            }
        }
    }

    // Handle the Up button press from the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
