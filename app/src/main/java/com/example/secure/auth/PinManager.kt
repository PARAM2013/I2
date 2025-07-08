package com.example.secure.auth

import android.content.Context
import android.content.SharedPreferences

object PinManager {

    private const val PREFS_NAME = "iSecure_PinPrefs"
    private const val PIN_KEY = "app_pin"
    private const val FINGERPRINT_ENABLED_KEY = "fingerprint_enabled"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun savePin(context: Context, pin: String) {
        val editor = getPreferences(context).edit()
        editor.putString(PIN_KEY, pin) // In a real app, hash the PIN before saving
        editor.apply()
    }

    fun getPin(context: Context): String? {
        return getPreferences(context).getString(PIN_KEY, null)
    }

    fun isPinSet(context: Context): Boolean {
        return getPreferences(context).contains(PIN_KEY)
    }

    fun verifyPin(context: Context, pinToVerify: String): Boolean {
        val storedPin = getPin(context)
        return storedPin != null && storedPin == pinToVerify
    }

    fun clearPin(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(PIN_KEY)
        editor.apply()
    }

    // Fingerprint preference
    fun setFingerprintAuthEnabled(context: Context, enabled: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(FINGERPRINT_ENABLED_KEY, enabled)
        editor.apply()
    }

    fun isFingerprintAuthEnabled(context: Context): Boolean {
        // Default to false if not explicitly set, but also check if a PIN is set,
        // as fingerprint usually relies on a PIN as a backup.
        return isPinSet(context) && getPreferences(context).getBoolean(FINGERPRINT_ENABLED_KEY, false)
    }
}
