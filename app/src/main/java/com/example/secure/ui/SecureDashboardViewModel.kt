package com.example.secure.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.secure.file.FileManager
import kotlinx.coroutines.launch
import java.io.File

data class SecureDashboardUiState(
    val stats: FileManager.VaultStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val fileOperationResult: String? = null // For toasts/messages after operations
)

class SecureDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData<SecureDashboardUiState>()
    val uiState: LiveData<SecureDashboardUiState> = _uiState

    private val fileManager = FileManager
    private val appContext: Context = application.applicationContext

    fun loadDashboardData() {
        _uiState.value = SecureDashboardUiState(isLoading = true)
        viewModelScope.launch {
            try {
                Log.d("SecureDashboardVM", "Loading vault data...")
                val stats = fileManager.listFilesInVault()
                _uiState.postValue(SecureDashboardUiState(stats = stats, isLoading = false))
                Log.d("SecureDashboardVM", "Vault data loaded. Folders: ${stats.grandTotalFolders}, Files: ${stats.grandTotalFiles}")
            } catch (e: Exception) {
                Log.e("SecureDashboardVM", "Error loading dashboard data", e)
                _uiState.postValue(SecureDashboardUiState(error = "Failed to load data: ${e.message}", isLoading = false))
            }
        }
    }

    fun importFile(uri: Uri, deleteOriginal: Boolean = true) {
        _uiState.value = _uiState.value?.copy(isLoading = true, fileOperationResult = null)
        viewModelScope.launch {
            try {
                Log.d("SecureDashboardVM", "Importing file: $uri")
                val importedFile = fileManager.importFile(uri, appContext, null, deleteOriginal)
                if (importedFile != null) {
                    _uiState.postValue(_uiState.value?.copy(isLoading = false, fileOperationResult = "File imported: ${importedFile.name}"))
                    loadDashboardData() // Refresh data
                } else {
                    _uiState.postValue(_uiState.value?.copy(isLoading = false, fileOperationResult = "Failed to import file. Check logs."))
                }
            } catch (e: Exception) {
                Log.e("SecureDashboardVM", "Error importing file", e)
                _uiState.postValue(_uiState.value?.copy(isLoading = false, fileOperationResult = "Error importing file: ${e.message}"))
            }
        }
    }

    fun createFolder(folderName: String) {
        _uiState.value = _uiState.value?.copy(isLoading = true, fileOperationResult = null)
        viewModelScope.launch {
            try {
                Log.d("SecureDashboardVM", "Creating folder: $folderName")
                val createdFolder = fileManager.createSubFolderInVault(folderName, null)
                if (createdFolder != null && createdFolder.exists()) {
                    _uiState.postValue(_uiState.value?.copy(isLoading = false, fileOperationResult = "Folder created: $folderName"))
                    loadDashboardData() // Refresh data
                } else {
                    val reason = if (createdFolder == null) "API returned null" else "Folder does not exist post-creation"
                    Log.e("SecureDashboardVM", "Failed to create folder. Reason: $reason. Name: $folderName")
                    _uiState.postValue(_uiState.value?.copy(isLoading = false, fileOperationResult = "Failed to create folder. Check logs."))
                }
            } catch (e: Exception) {
                Log.e("SecureDashboardVM", "Error creating folder", e)
                _uiState.postValue(_uiState.value?.copy(isLoading = false, fileOperationResult = "Error creating folder: ${e.message}"))
            }
        }
    }

    // Call this to clear one-time messages like toasts after they've been shown
    fun clearFileOperationResult() {
        _uiState.value = _uiState.value?.copy(fileOperationResult = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value?.copy(error = null)
    }
}
