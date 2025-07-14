package com.example.secure.ui.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secure.file.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMediaFiles()
    }

    fun loadMediaFiles(filter: Filter = Filter.ALL) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allFiles = FileManager.getAllFiles()
            val mediaFiles = allFiles.filter {
                it.category == FileManager.FileCategory.IMAGE || it.category == FileManager.FileCategory.VIDEO
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    mediaFiles = mediaFiles,
                    filteredMediaFiles = when (filter) {
                        Filter.ALL -> mediaFiles
                        Filter.IMAGES -> mediaFiles.filter { file -> file.category == FileManager.FileCategory.IMAGE }
                        Filter.VIDEOS -> mediaFiles.filter { file -> file.category == FileManager.FileCategory.VIDEO }
                    }
                )
            }
        }
    }
}

data class GalleryUiState(
    val isLoading: Boolean = true,
    val mediaFiles: List<FileManager.VaultFile> = emptyList(),
    val filteredMediaFiles: List<FileManager.VaultFile> = emptyList()
)

enum class Filter {
    ALL,
    IMAGES,
    VIDEOS
}
