package com.example.secure.util

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {

    private const val PREFS_NAME = "iSecure_prefs"
    private const val KEY_LAST_PATH = "key_last_path"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLastPath(context: Context): String? {
        return getPreferences(context).getString(KEY_LAST_PATH, null)
    }

    fun setLastPath(context: Context, path: String?) {
        val editor = getPreferences(context).edit()
        if (path == null) {
            editor.remove(KEY_LAST_PATH)
        } else {
            editor.putString(KEY_LAST_PATH, path)
        }
        editor.apply()
    }
}
