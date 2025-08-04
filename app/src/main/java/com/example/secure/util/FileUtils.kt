package com.example.secure.util

import java.text.DecimalFormat

object FileUtils {
    fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun formatDuration(durationInMillis: Long): String {
        val seconds = (durationInMillis / 1000) % 60
        val minutes = (durationInMillis / (1000 * 60)) % 60
        val hours = (durationInMillis / (1000 * 60 * 60))

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
