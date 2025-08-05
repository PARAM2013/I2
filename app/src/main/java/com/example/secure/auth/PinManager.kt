package com.example.secure.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinManager {

    private const val PREFS_NAME = "iSecure_PinPrefs"
    private const val PIN_KEY = "app_pin_hash" // Changed to store hash
    private const val SALT_KEY = "app_pin_salt" // Key for storing the salt
    private const val FINGERPRINT_ENABLED_KEY = "fingerprint_enabled"
    private const val METADATA_REMOVAL_ENABLED_KEY = "metadata_removal_enabled" // New key
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun savePin(context: Context, pin: String) {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded

        getPreferences(context).edit {
            putString(PIN_KEY, hash.toHexString())
            putString(SALT_KEY, salt.toHexString())
        }
    }

    fun isPinSet(context: Context): Boolean {
        return getPreferences(context).contains(PIN_KEY)
    }

    fun verifyPin(context: Context, pinToVerify: String): Boolean {
        val storedHashHex = getPreferences(context).getString(PIN_KEY, null)
        val storedSaltHex = getPreferences(context).getString(SALT_KEY, null)

        if (storedHashHex == null || storedSaltHex == null) {
            return false
        }

        val salt = storedSaltHex.hexToByteArray()
        val storedHash = storedHashHex.hexToByteArray()

        val spec = PBEKeySpec(pinToVerify.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hashToVerify = factory.generateSecret(spec).encoded

        return storedHash.contentEquals(hashToVerify)
    }

    // Fingerprint preference
    fun setFingerprintAuthEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit {
            putBoolean(FINGERPRINT_ENABLED_KEY, enabled)
        }
    }

    fun isFingerprintAuthEnabled(context: Context): Boolean {
        // Default to false if not explicitly set, but also check if a PIN is set,
        // as fingerprint usually relies on a PIN as a backup.
        return isPinSet(context) && getPreferences(context).getBoolean(FINGERPRINT_ENABLED_KEY, false)
    }

    // Metadata removal preference
    fun setMetadataRemovalEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit {
            putBoolean(METADATA_REMOVAL_ENABLED_KEY, enabled)
        }
    }

    fun isMetadataRemovalEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(METADATA_REMOVAL_ENABLED_KEY, false) // Default to false
    }

    // Helper functions for hex conversion
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun String.hexToByteArray(): ByteArray {
        return ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}
