package com.example.secure.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SortManager {

    private const val PREFS_NAME = "sort_prefs"
    private const val KEY_SORT_OPTION = "sort_option"

    enum class SortOption {
        DATE_DESC,
        DATE_ASC,
        SIZE_DESC,
        SIZE_ASC
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSortOption(context: Context, sortOption: SortOption) {
        getPreferences(context).edit {
            putString(KEY_SORT_OPTION, sortOption.name)
        }
    }

    fun getSortOption(context: Context): SortOption {
        val sortOptionName = getPreferences(context).getString(KEY_SORT_OPTION, SortOption.DATE_DESC.name)
        return SortOption.valueOf(sortOptionName ?: SortOption.DATE_DESC.name)
    }
}
