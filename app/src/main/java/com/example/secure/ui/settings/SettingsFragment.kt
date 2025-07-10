package com.example.secure.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.secure.R
import com.example.secure.auth.PinManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // 1. Change App PIN
        findPreference<Preference>("key_change_pin")?.setOnPreferenceClickListener {
            activity?.let {
                val intent = Intent(it, PinChangeActivity::class.java)
                startActivity(intent)
            }
            true
        }

        // 2. Enable/Disable Fingerprint Unlock
        findPreference<SwitchPreferenceCompat>("key_fingerprint_unlock")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                activity?.let { PinManager.setFingerprintAuthEnabled(it, enabled) }
                true
            }
            // Set initial checked state based on current settings
            isChecked = activity?.let { PinManager.isFingerprintAuthEnabled(it) } ?: false
        }

        // 3. Enable/Disable Metadata Removal
        findPreference<SwitchPreferenceCompat>("key_metadata_removal")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                activity?.let { PinManager.setMetadataRemovalEnabled(it, enabled) }
                true
            }
            isChecked = activity?.let { PinManager.isMetadataRemovalEnabled(it) } ?: false
        }
    }
}
