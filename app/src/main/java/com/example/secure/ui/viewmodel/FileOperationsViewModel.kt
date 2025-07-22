package com.example.secure.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class FileOperationState(
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0
)

class FileOperationsViewModel : ViewModel() {
    var state by mutableStateOf(FileOperationState())
        private set

    fun startProcessing(totalFiles: Int) {
        state = FileOperationState(
            isProcessing = true,
            totalFiles = totalFiles,
            processedFiles = 0,
            progress = 0f
        )
    }

    fun updateProgress(processedFiles: Int) {
        state = state.copy(
            processedFiles = processedFiles,
            progress = if (state.totalFiles > 0) {
                processedFiles.toFloat() / state.totalFiles.toFloat()
            } else {
                0f
            }
        )
    }

    fun endProcessing() {
        state = state.copy(isProcessing = false)
    }
}
