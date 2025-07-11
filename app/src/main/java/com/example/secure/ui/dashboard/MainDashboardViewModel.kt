package com.example.secure.ui.dashboard

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secure.file.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class DashboardCategoryItem(
    val id: String,
    val title: String,
    var subtitle: String, // Made var to be updatable
    val icon: ImageVector
)

data class MainDashboardUiState(
    val categories: List<DashboardCategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val fileOperationResult: String? = null, // For toasts/messages
    val showCreateFolderDialog: Boolean = false,
    val vaultStats: FileManager.VaultStats? = null // To hold all stats
)

class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainDashboardUiState())
    val uiState: StateFlow<MainDashboardUiState> = _uiState.asStateFlow()

    private val fileManager = FileManager
    private val appContext: Context = application.applicationContext

    // Predefined category structure
    private val predefinedCategories = listOf(
        DashboardCategoryItem("all_files", "All Files", "", Icons.Filled.Folder),
        DashboardCategoryItem("images", "Images", "", Icons.Filled.Image),
        DashboardCategoryItem("videos", "Videos", "", Icons.Filled.Videocam),
        DashboardCategoryItem("documents", "Documents", "", Icons.Filled.Article)
    )

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                Log.d("MainDashboardVM", "Loading vault data...")
                val stats = fileManager.listFilesInVault()
                Log.d("MainDashboardVM", "Vault data loaded. Folders: ${stats.grandTotalFolders}, Files: ${stats.grandTotalFiles}")
                val updatedCategories = predefinedCategories.map { category ->
                    when (category.id) {
                        "all_files" -> category.copy(subtitle = formatAllFilesSubtitle(stats))
                        "images" -> category.copy(subtitle = formatCategorySubtitle(stats.totalPhotoFiles, stats.totalPhotoSize))
                        "videos" -> category.copy(subtitle = formatCategorySubtitle(stats.totalVideoFiles, stats.totalVideoSize))
                        "documents" -> category.copy(subtitle = formatCategorySubtitle(stats.totalDocumentFiles, stats.totalDocumentSize))
                        else -> category
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categories = updatedCategories,
                        vaultStats = stats,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading dashboard data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}",
                        categories = predefinedCategories.map { cat -> cat.copy(subtitle = "Error loading data") } // Show error in subtitles
                    )
                }
            }
        }
    }

    private fun formatAllFilesSubtitle(stats: FileManager.VaultStats): String {
        return String.format(Locale.getDefault(),
            "%d Folders, %d Files, %s",
            stats.grandTotalFolders,
            stats.grandTotalFiles,
            formatSize(stats.grandTotalSize)
        )
    }

    private fun formatCategorySubtitle(fileCount: Int, totalSize: Long): String {
        return String.format(Locale.getDefault(),
            "%d Files, %s",
            fileCount,
            formatSize(totalSize)
        )
    }

    private fun formatSize(sizeBytes: Long): String {
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1 -> String.format(Locale.getDefault(), "%.1f GB", gb)
            mb >= 1 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            kb >= 1 -> String.format(Locale.getDefault(), "%.1f KB", kb)
            else -> String.format(Locale.getDefault(), "%d Bytes", sizeBytes)
        }
    }

    fun importFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.update { it.copy(isLoading = true, fileOperationResult = null) }
        viewModelScope.launch {
            var successfulImports = 0
            var failedImports = 0
            uris.forEach { uri ->
                try {
                    Log.d("MainDashboardVM", "Importing file: $uri")
                    // Assuming deleteOriginal = true by default, adjust if needed
                    val importedFile = fileManager.importFile(uri, appContext, null, true)
                    if (importedFile != null) {
                        successfulImports++
                    } else {
                        failedImports++
                    }
                } catch (e: Exception) {
                    Log.e("MainDashboardVM", "Error importing file $uri", e)
                    failedImports++
                }
            }
            val message = when {
                successfulImports > 0 && failedImports == 0 -> "$successfulImports file(s) imported successfully."
                successfulImports > 0 && failedImports > 0 -> "$successfulImports file(s) imported, $failedImports failed."
                failedImports > 0 -> "Failed to import $failedImports file(s)."
                else -> "No files were imported."
            }
            _uiState.update { it.copy(isLoading = false, fileOperationResult = message) }
            loadDashboardData() // Refresh data
        }
    }

    fun createFolder(folderName: String) {
        _uiState.update { it.copy(isLoading = true, fileOperationResult = null, showCreateFolderDialog = false) }
        viewModelScope.launch {
            try {
                Log.d("MainDashboardVM", "Creating folder: $folderName")
                val createdFolder = fileManager.createSubFolderInVault(folderName, null)
                if (createdFolder != null && createdFolder.exists()) {
                    _uiState.update { it.copy(isLoading = false, fileOperationResult = "Folder created: $folderName") }
                    loadDashboardData() // Refresh data
                } else {
                    val reason = if (createdFolder == null) "API returned null" else "Folder does not exist post-creation"
                    Log.e("MainDashboardVM", "Failed to create folder. Reason: $reason. Name: $folderName")
                    _uiState.update { it.copy(isLoading = false, fileOperationResult = "Failed to create folder. Check logs.") }
                }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error creating folder", e)
                _uiState.update { it.copy(isLoading = false, fileOperationResult = "Error creating folder: ${e.message}") }
            }
        }
    }

    fun requestCreateFolderDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateFolderDialog = show) }
    }

    fun clearFileOperationResult() {
        _uiState.update { it.copy(fileOperationResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
