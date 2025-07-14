package com.example.secure.ui.dashboard

import android.app.Application
import com.example.secure.R // Added missing import for R class
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
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
    val vaultStats: FileManager.VaultStats? = null, // To hold all stats
    val imageFiles: List<FileManager.VaultFile> = emptyList(),
    val videoFiles: List<FileManager.VaultFile> = emptyList(),
    val documentFiles: List<FileManager.VaultFile> = emptyList()
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
        DashboardCategoryItem("documents", "Documents", "", Icons.AutoMirrored.Filled.Article)
    )

    init {
        loadGlobalDashboardCategories() // For the main dashboard screen
        navigateToPath(null)      // For AllFilesScreen content (root initially)
    }

    // Loads global stats for the main dashboard categories
    private fun loadGlobalDashboardCategories() {
        viewModelScope.launch {
            try {
                val allFiles = fileManager.listAllFilesRecursively(FileManager.getVaultDirectory())
                val imageFiles = allFiles.filter { it.category == FileManager.FileCategory.PHOTO }
                val videoFiles = allFiles.filter { it.category == FileManager.FileCategory.VIDEO }
                val documentFiles = allFiles.filter { it.category == FileManager.FileCategory.DOCUMENT }
                val allFolders = fileManager.listAllFoldersRecursively(FileManager.getVaultDirectory())

                val totalImageSize = imageFiles.sumOf { it.size }
                val totalVideoSize = videoFiles.sumOf { it.size }
                val totalDocumentSize = documentFiles.sumOf { it.size }
                val grandTotalSize = allFiles.sumOf { it.size }

                val updatedGlobalCategories = globalCategoriesTemplate.map { category ->
                    when (category.id) {
                        "all_files" -> category.copy(subtitle = formatAllFilesSubtitle(allFolders.size, allFiles.size, grandTotalSize))
                        "images" -> category.copy(subtitle = formatCategorySubtitle(imageFiles.size, totalImageSize))
                        "videos" -> category.copy(subtitle = formatCategorySubtitle(videoFiles.size, totalVideoSize))
                        "documents" -> category.copy(subtitle = formatCategorySubtitle(documentFiles.size, totalDocumentSize))
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

    fun loadAllVideos() {
        _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous error
        viewModelScope.launch {
            try {
                val allFiles = fileManager.listAllFilesRecursively(FileManager.getVaultDirectory())
                val videoFiles = allFiles.filter { it.category == FileManager.FileCategory.VIDEO }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        videoFiles = videoFiles,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading all videos", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load videos: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadAllDocuments() {
        _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous error
        viewModelScope.launch {
            try {
                val allFiles = fileManager.listAllFilesRecursively(FileManager.getVaultDirectory())
                val documentFiles = allFiles.filter { it.category == FileManager.FileCategory.DOCUMENT }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        documentFiles = documentFiles,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading all documents", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load documents: ${e.message}"
                    )
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
                        imageFiles = pathStats.allFiles.filter { file -> file.category == FileManager.FileCategory.PHOTO },
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

    fun loadAllImages() {
        _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous error
        viewModelScope.launch {
            try {
                val allFiles = fileManager.listAllFilesRecursively(FileManager.getVaultDirectory())
                val imageFiles = allFiles.filter { it.category == FileManager.FileCategory.PHOTO }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        imageFiles = imageFiles,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading all images", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load images: ${e.message}"
                    )
                }
            }
        }
    }

    private fun formatAllFilesSubtitle(folders: Int, files: Int, size: Long): String {
        return String.format(
            Locale.getDefault(),
            "%d Folders, %d Files, %s",
            folders,
            files,
            formatSize(size)
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
            val itemName: String
            var operationMessage: String

            when (item) {
                is FileManager.VaultFile -> {
                    itemName = item.file.name
                    Log.d("MainDashboardVM", "Requesting unhide for file: $itemName")
                    val unhiddenFile = fileManager.unhideFile(item.file, appContext, FileManager.getUnhideDirectory())
                    operationMessage = if (unhiddenFile != null) {
                        appContext.getString(R.string.file_restored_success) + " ${unhiddenFile?.name}"
                    } else {
                        "Failed to unhide file: $itemName"
                    }
                    if (unhiddenFile != null) navigateToPath(_currentPath.value)
                }
                is FileManager.VaultFolder -> {
                    itemName = item.folder.name
                    Log.d("MainDashboardVM", "Requesting unhide for folder: $itemName")
                    val success = fileManager.unhideFolderRecursive(item.folder, appContext, FileManager.getUnhideDirectory())
                    operationMessage = if (success) {
                        appContext.getString(R.string.file_restored_success) + " Folder: $itemName" // Consider a more folder-specific string
                    } else {
                        "Failed to unhide folder: $itemName"
                    }
                    if (success) navigateToPath(_currentPath.value)
                }
                else -> {
                    Log.w("MainDashboardVM", "requestUnhideItem: Unknown item type")
                    operationMessage = "Unhide failed: Unknown item type."
                }
            }

            _uiState.update { it.copy(isLoading = false, fileOperationMessage) }
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

            if (fileToDelete != null) {
                Log.d("MainDashboardVM", "Requesting delete for: ${fileToDelete.name}")
                val success = fileManager.deleteFileFromVault(fileToDelete)
                operationMessage = if (success) {
                    appContext.getString(R.string.file_delete_success) + " ${fileToDelete.name}"
                } else {
                    "Failed to delete: ${fileToDelete.name}"
                }
                if (success) navigateToPath(_currentPath.value)
            } else {
                Log.w("MainDashboardVM", "requestDeleteItem: Unknown item type")
                operationMessage = "Delete failed: Unknown item type."
            }

            _uiState.update { it.copy(isLoading = false, fileOperationResult = operationMessage) }
        }
    }

    fun requestRenameItem(item: Any, newName: String) {
        _uiState.update { it.copy(isLoading = true, fileOperationResult = null) }
        viewModelScope.launch {
            val fileToRename: File? = when (item) {
                is FileManager.VaultFile -> item.file
                is FileManager.VaultFolder -> item.folder
                else -> null
            }
            val oldName: String = fileToRename?.name ?: "Item"

            var operationMessage: String

            if (fileToRename == null) {
                operationMessage = "Rename failed: Unknown item type."
                Log.w("MainDashboardVM", "requestRenameItem: Unknown item type")
            } else if (newName.isBlank()) {
                operationMessage = appContext.getString(R.string.folder_name_empty_error) // Reusing existing string
            } else if (newName == oldName) {
                operationMessage = "New name is the same as the old name." // Or no message if it's a silent no-op
            }
            else {
                val renamedFile = fileManager.renameItemInVault(fileToRename, newName, appContext)
                if (renamedFile != null) {
                    operationMessage = appContext.getString(R.string.rename_success, oldName, newName)
                } else {
                    // FileManager.renameItemInVault logs specific errors and returns null for various reasons.
                    // We check some common reasons here for more specific feedback.
                    val newFileCheck = File(fileToRename.parentFile, newName)
                    if (newFileCheck.exists()){
                        operationMessage = appContext.getString(R.string.rename_failed_exists, newName)
                    } else if (newName.contains(File.separatorChar) || newName == "." || newName == ".." || newName.any { it in "\\:*?\"<>|" }) { // Added more invalid chars check
                        operationMessage = appContext.getString(R.string.error_invalid_name, newName)
                    }
                    else {
                        operationMessage = appContext.getString(R.string.rename_failed, oldName)
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false, fileOperationResult = operationMessage) }
            // Always refresh, even on failure, to ensure UI consistency or clear selections.
            // If success, this will show the renamed item.
            navigateToPath(_currentPath.value)
        }
    }

    fun shareFile(file: FileManager.VaultFile) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "com.example.secure.provider",
                    file.file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = context.contentResolver.getType(uri)
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = android.content.Intent.createChooser(intent, "Share File")
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error sharing file", e)
                _uiState.update {
                    it.copy(
                        error = "Failed to share file: ${e.message}"
                    )
                }
            }
        }
    }
}
