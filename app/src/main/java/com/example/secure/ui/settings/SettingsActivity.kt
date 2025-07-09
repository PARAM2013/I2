package com.example.secure.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.secure.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings) // We'll create this layout next

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Handle the Up button press
    override fun onSupportNavigateUp(): Boolean {
        finish() // or super.onSupportNavigateUp() if you have more complex navigation
        return true
    }
}
