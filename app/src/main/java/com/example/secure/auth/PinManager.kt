package com.example.secure.auth

import android.content.Context
import android.content.SharedPreferences
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinManager {

    private const val PREFS_NAME = "iSecure_PinPrefs"
    private const val PIN_KEY = "app_pin_hash" // Changed to store hash
    private const val SALT_KEY = "app_pin_salt" // Key for storing the salt
    private const val FINGERPRINT_ENABLED_KEY = "fingerprint_enabled"
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

        val editor = getPreferences(context).edit()
        editor.putString(PIN_KEY, hash.toHexString())
        editor.putString(SALT_KEY, salt.toHexString())
        editor.apply()
    }

    fun getPin(context: Context): String? {
        // This method is no longer safe or useful as it would return the hash.
        // It's better to remove it or make it private if only used internally.
        // For now, it will return the hash, but it shouldn't be used for verification.
        return getPreferences(context).getString(PIN_KEY, null)
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

    fun clearPin(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(PIN_KEY)
        editor.remove(SALT_KEY)
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

    // Helper functions for hex conversion
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun String.hexToByteArray(): ByteArray {
        return ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }
}
