package com.example.secure.utils

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
    }
}
