package com.example.secure.ui.dashboard

import android.app.Application
import com.example.secure.R // Added missing import for R class
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
import java.io.File // Added missing import
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

    private val _currentPath = MutableStateFlow<String?>(null) // null represents vault root
    val currentPath: StateFlow<String?> = _currentPath.asStateFlow()

    private val fileManager = FileManager
    private val appContext: Context = application.applicationContext

    // Predefined category structure - these will reflect GLOBAL stats
    private val globalCategoriesTemplate = listOf(
        DashboardCategoryItem("all_files", "All Files", "", Icons.Filled.Folder),
        DashboardCategoryItem("images", "Images", "", Icons.Filled.Image),
        DashboardCategoryItem("videos", "Videos", "", Icons.Filled.Videocam),
        DashboardCategoryItem("documents", "Documents", "", Icons.Filled.Article)
    )

    init {
        loadGlobalDashboardCategories() // For the main dashboard screen
        navigateToPath(null)      // For AllFilesScreen content (root initially)
    }

    // Loads global stats for the main dashboard categories
    private fun loadGlobalDashboardCategories() {
        viewModelScope.launch {
            try {
                val globalStats = fileManager.listFilesInVault(FileManager.getVaultDirectory()) // Explicitly list root for global stats
                val updatedGlobalCategories = globalCategoriesTemplate.map { category ->
                    when (category.id) {
                        "all_files" -> category.copy(subtitle = formatAllFilesSubtitle(globalStats))
                        "images" -> category.copy(subtitle = formatCategorySubtitle(globalStats.totalPhotoFiles, globalStats.totalPhotoSize))
                        "videos" -> category.copy(subtitle = formatCategorySubtitle(globalStats.totalVideoFiles, globalStats.totalVideoSize))
                        "documents" -> category.copy(subtitle = formatCategorySubtitle(globalStats.totalDocumentFiles, globalStats.totalDocumentSize))
                        else -> category
                    }
                }
                _uiState.update { it.copy(categories = updatedGlobalCategories) }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading global category stats", e)
                _uiState.update { oldState ->
                    oldState.copy(categories = globalCategoriesTemplate.map { it.copy(subtitle = "Error") })
                }
            }
        }
    }

    // Loads contents for a specific path (for AllFilesScreen)
    private fun loadPathContents(relativePath: String?) {
        _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous error
        viewModelScope.launch {
            try {
                val targetDirectory = if (relativePath.isNullOrBlank()) {
                    FileManager.getVaultDirectory()
                } else {
                    File(FileManager.getVaultDirectory(), relativePath)
                }
                Log.d("MainDashboardVM", "Loading contents for path: ${targetDirectory.absolutePath}")
                val pathStats = fileManager.listFilesInVault(targetDirectory)
                Log.d("MainDashboardVM", "Path data loaded. Folders: ${pathStats.allFolders.size}, Files: ${pathStats.allFiles.size} for $relativePath")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        vaultStats = pathStats, // This state is for AllFilesScreen
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading path contents for $relativePath", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load contents for '$relativePath': ${e.message}",
                        vaultStats = FileManager.VaultStats() // Clear stats on error
                    )
                }
            }
        }
    }

    fun navigateToPath(relativePath: String?) {
        val newPath = if (relativePath.isNullOrBlank() || relativePath == File.separator) null else relativePath
        _currentPath.value = newPath
        loadPathContents(newPath)
        // Refresh global categories as operations like delete/import in subfolders affect global counts
        loadGlobalDashboardCategories()
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
                    Log.d("MainDashboardVM", "Importing file: $uri to path: ${_currentPath.value}")
                    // Assuming deleteOriginal = true by default, adjust if needed
                    val importedFile = fileManager.importFile(uri, appContext, _currentPath.value, true)
                    if (importedFile != null) {
                        successfulImports++
                    } else {
                        failedImports++
                    }
                } catch (e: Exception) {
                    Log.e("MainDashboardVM", "Error importing file $uri to path: ${_currentPath.value}", e)
                    failedImports++
                }
            }
            val message = when {
                successfulImports > 0 && failedImports == 0 -> "$successfulImports file(s) imported successfully."
                successfulImports > 0 && failedImports > 0 -> "$successfulImports file(s) imported, $failedImports failed."
                // failedImports > 0 -> "Failed to import $failedImports file(s)." // Covered by 'else' if successfulImports is 0
                else -> "No files were imported, or all failed. Check logs if files were selected." // Adjusted message
            }
            _uiState.update { it.copy(isLoading = false, fileOperationResult = message) }
            navigateToPath(_currentPath.value) // Refresh current path and global categories
        }
    }

    fun createFolder(folderName: String) {
        _uiState.update { it.copy(isLoading = true, fileOperationResult = null, showCreateFolderDialog = false) }
        viewModelScope.launch {
            try {
                Log.d("MainDashboardVM", "Creating folder: $folderName in path: ${_currentPath.value}")
                val createdFolder = fileManager.createSubFolderInVault(folderName, _currentPath.value)
                if (createdFolder != null && createdFolder.exists()) {
                    _uiState.update { it.copy(isLoading = false, fileOperationResult = "Folder created: $folderName") }
                    navigateToPath(_currentPath.value) // Refresh current path and global categories
                } else {
                    val reason = if (createdFolder == null) "API returned null" else "Folder does not exist post-creation or name invalid"
                    Log.e("MainDashboardVM", "Failed to create folder '$folderName'. Reason: $reason.")
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

    fun requestUnhideItem(item: Any) {
        _uiState.update { it.copy(isLoading = true, fileOperationResult = null) }
        viewModelScope.launch {
            val success: Boolean
            val itemName: String
            var operationMessage: String

            when (item) {
                is FileManager.VaultFile -> {
                    itemName = item.file.name
                    Log.d("MainDashboardVM", "Requesting unhide for file: $itemName")
                    val unhiddenFile = fileManager.unhideFile(item.file, appContext, FileManager.getUnhideDirectory())
                    success = unhiddenFile != null
                    operationMessage = if (success) {
                        appContext.getString(R.string.file_restored_success) + " ${unhiddenFile?.name}"
                    } else {
                        "Failed to unhide file: $itemName"
                    }
                }
                is FileManager.VaultFolder -> {
                    itemName = item.folder.name
                    Log.d("MainDashboardVM", "Requesting unhide for folder: $itemName")
                    success = fileManager.unhideFolderRecursive(item.folder, appContext, FileManager.getUnhideDirectory())
                    operationMessage = if (success) {
                        appContext.getString(R.string.file_restored_success) + " Folder: $itemName" // Consider a more folder-specific string
                    } else {
                        "Failed to unhide folder: $itemName"
                    }
                }
                else -> {
                    Log.w("MainDashboardVM", "requestUnhideItem: Unknown item type")
                    success = false
                    operationMessage = "Unhide failed: Unknown item type."
                }
            }

            _uiState.update { it.copy(isLoading = false, fileOperationResult = operationMessage) }
            if (success) {
                navigateToPath(_currentPath.value) // Refresh content
            }
        }
    }

    fun requestDeleteItem(item: Any) {
        _uiState.update { it.copy(isLoading = true, fileOperationResult = null) }
        viewModelScope.launch {
            val fileToDelete: File? = when (item) {
                is FileManager.VaultFile -> item.file
                is FileManager.VaultFolder -> item.folder
                else -> null
            }

            var operationMessage: String
            var success = false

            if (fileToDelete != null) {
                Log.d("MainDashboardVM", "Requesting delete for: ${fileToDelete.name}")
                success = fileManager.deleteFileFromVault(fileToDelete)
                operationMessage = if (success) {
                    appContext.getString(R.string.file_delete_success) + " ${fileToDelete.name}"
                } else {
                    "Failed to delete: ${fileToDelete.name}"
                }
            } else {
                Log.w("MainDashboardVM", "requestDeleteItem: Unknown item type")
                operationMessage = "Delete failed: Unknown item type."
            }

            _uiState.update { it.copy(isLoading = false, fileOperationResult = operationMessage) }
            if (success) {
                navigateToPath(_currentPath.value) // Refresh content
            }
        }
    }
}
