package com.example.secure.ui.dashboard

// Placeholder for file data model
data class VaultFile(
    val name: String,
    val isFolder: Boolean,
    val type: FileType = FileType.OTHER // Default to OTHER
)

enum class FileType {
    FOLDER, IMAGE, VIDEO, DOCUMENT, OTHER
}
