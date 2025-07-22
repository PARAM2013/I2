package com.example.secure.ui.viewmodel

import java.io.File

data class FileListState(
    val selectedFiles: Set<File> = emptySet(),
    val isSelectionMode: Boolean = false
)
