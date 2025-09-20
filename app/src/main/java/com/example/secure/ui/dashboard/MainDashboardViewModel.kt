package com.example.secure.ui.dashboard

import android.app.Application
import com.example.secure.R // Added missing import for R class
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secure.file.FileManager
import com.example.secure.util.AppPreferences
import com.example.secure.util.SortManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.io.File
import java.util.Locale

data class DashboardCategoryItem(
    val id: String,
    val title: String,
    var subtitle: String, // Made var to be updatable
    val iconResId: Int, // For drawable resources
    val thumbnail: android.graphics.Bitmap? = null // For video thumbnails
)

data class ImportProgress(
    val isImporting: Boolean = false,
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val currentFileName: String = "",
    val overallProgress: Float = 0f,
    val successfulImports: Int = 0,
    val failedImports: Int = 0,
    val canCancel: Boolean = true,
    val isCancelled: Boolean = false
)

data class UnhideProgress(
    val isUnhiding: Boolean = false,
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val currentFileName: String = "",
    val overallProgress: Float = 0f,
    val successfulUnhides: Int = 0,
    val failedUnhides: Int = 0,
    val canCancel: Boolean = true,
    val isCancelled: Boolean = false
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
    val documentFiles: List<FileManager.VaultFile> = emptyList(),
    val isSelectionModeActive: Boolean = false,
    val selectedItems: Set<Any> = emptySet(),
    val showDeleteConfirmation: Boolean = false,
    val showUnhideConfirmation: Boolean = false,
    val sortOption: SortManager.SortOption = SortManager.SortOption.DATE_DESC,
    val importProgress: ImportProgress = ImportProgress(),
    val showImportSuccessDialog: Boolean = false,
    val lastImportSuccessCount: Int = 0,
    val lastImportFailedCount: Int = 0,
    val unhideProgress: UnhideProgress = UnhideProgress(),
    val showUnhideSuccessDialog: Boolean = false,
    val lastUnhideSuccessCount: Int = 0,
    val lastUnhideFailedCount: Int = 0
)

class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainDashboardUiState())
    val uiState: StateFlow<MainDashboardUiState> = _uiState.asStateFlow()

    private val _currentPath = MutableStateFlow<String?>(null) // null represents vault root
    val currentPath: StateFlow<String?> = _currentPath.asStateFlow()

    private val fileManager = FileManager
    private val appContext: Context = application.applicationContext
    
    private var importJob: kotlinx.coroutines.Job? = null
    private var unhideJob: kotlinx.coroutines.Job? = null

    // Predefined category structure - these will reflect GLOBAL stats
    private val globalCategoriesTemplate = listOf(
        DashboardCategoryItem("all_files", "All Files", "", R.drawable.ic_folder),
        DashboardCategoryItem("images", "Images", "", R.drawable.ic_image),
        DashboardCategoryItem("videos", "Videos", "", R.drawable.ic_video),
        DashboardCategoryItem("documents", "Documents", "", R.drawable.ic_document)
    )

    init {
        _uiState.update { it.copy(sortOption = SortManager.getSortOption(appContext)) }
        loadGlobalDashboardCategories() // For the main dashboard screen
        val lastPath = AppPreferences.getLastPath(application)
        navigateToPath(lastPath)      // For AllFilesScreen content (restore last path)
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
                        "videos" -> category.copy(
                            subtitle = formatCategorySubtitle(videoFiles.size, totalVideoSize),
                            thumbnail = videoFiles.firstOrNull()?.thumbnail // Get thumbnail from first video
                        )
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
                val videoFilesWithThumbnails = allFiles
                    .filter { it.category == FileManager.FileCategory.VIDEO }
                    .map { vaultFile ->
                        val thumbnail = try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                android.media.ThumbnailUtils.createVideoThumbnail(vaultFile.file, android.util.Size(256, 256), null)
                            } else {
                                android.media.ThumbnailUtils.createVideoThumbnail(
                                    vaultFile.file.path,
                                    MediaStore.Video.Thumbnails.MINI_KIND
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("MainDashboardVM", "Error creating thumbnail for ${vaultFile.file.name}", e)
                            null
                        }
                        vaultFile.copy(thumbnail = thumbnail, duration = fileManager.getVideoDuration(vaultFile.file))
                    }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        videoFiles = videoFilesWithThumbnails,
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
        if (!fileManager.checkStoragePermissions(appContext)) {
            _uiState.update { it.copy(
                error = "Storage permissions are required to view files. Please grant permissions in Settings.",
                isLoading = false
            ) }
            return
        }

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

                val sortedFiles = when (uiState.value.sortOption) {
                    SortManager.SortOption.DATE_DESC -> pathStats.allFiles.sortedByDescending { it.file.lastModified() }
                    SortManager.SortOption.DATE_ASC -> pathStats.allFiles.sortedBy { it.file.lastModified() }
                    SortManager.SortOption.SIZE_DESC -> pathStats.allFiles.sortedByDescending { it.size }
                    SortManager.SortOption.SIZE_ASC -> pathStats.allFiles.sortedBy { it.size }
                }.toMutableList()

                Log.d("MainDashboardVM", "Path data loaded. Folders: ${pathStats.allFolders.size}, Files: ${sortedFiles.size} for $relativePath")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        vaultStats = pathStats.copy(allFiles = sortedFiles),
                        imageFiles = sortedFiles.filter { file -> file.category == FileManager.FileCategory.PHOTO },
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

    fun setSortOption(sortOption: SortManager.SortOption) {
        SortManager.saveSortOption(appContext, sortOption)
        _uiState.update { it.copy(sortOption = sortOption) }
        navigateToPath(currentPath.value) // Reload with new sort order
    }

    fun navigateToPath(relativePath: String?) {
        val newPath = if (relativePath.isNullOrBlank() || relativePath == File.separator) null else relativePath
        _currentPath.value = newPath
        AppPreferences.setLastPath(getApplication(), newPath) // Save the path
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
        Log.d("MainDashboardVM", "importFiles called with ${uris.size} files")
        if (uris.isEmpty()) return

        if (!fileManager.checkStoragePermissions(appContext)) {
            _uiState.update { it.copy(
                error = "Storage permissions are required to import files. Please grant permissions in Settings.",
                isLoading = false
            ) }
            return
        }

        // Cancel any existing import job
        importJob?.cancel()

        Log.d("MainDashboardVM", "Starting import progress dialog")
        // Initialize import progress
        _uiState.update { 
            it.copy(
                isLoading = true, 
                fileOperationResult = null,
                importProgress = ImportProgress(
                    isImporting = true,
                    totalFiles = uris.size,
                    canCancel = true
                )
            ) 
        }
        Log.d("MainDashboardVM", "Import progress state updated: isImporting=true, totalFiles=${uris.size}")
        
        // Log current state to verify dialog should show
        Log.d("MainDashboardVM", "Current importProgress.isImporting: ${_uiState.value.importProgress.isImporting}")

        importJob = viewModelScope.launch {
            var successfulImports = 0
            var failedImports = 0

            // Add minimum delay to ensure dialog is visible
            kotlinx.coroutines.delay(500) // Half second minimum display

            uris.forEachIndexed { index, uri ->
                // Check if job was cancelled
                if (!coroutineContext.isActive) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            importProgress = ImportProgress(isCancelled = true),
                            fileOperationResult = "Import cancelled by user."
                        ) 
                    }
                    return@launch
                }

                try {
                    // Get file name for progress display
                    val fileName = getFileNameFromUri(uri) ?: "Unknown file"
                    
                    // Update progress
                    Log.d("MainDashboardVM", "Processing file ${index + 1}/${uris.size}: $fileName")
                    _uiState.update { 
                        it.copy(
                            importProgress = it.importProgress.copy(
                                currentFileIndex = index + 1,
                                currentFileName = fileName,
                                overallProgress = (index.toFloat() / uris.size),
                                successfulImports = successfulImports,
                                failedImports = failedImports
                            )
                        ) 
                    }
                    Log.d("MainDashboardVM", "Progress updated: ${index + 1}/${uris.size}, progress=${(index.toFloat() / uris.size)}")

                    Log.d("MainDashboardVM", "Importing file: $uri to path: ${_currentPath.value}")
                    val importResult = fileManager.importFile(uri, appContext, _currentPath.value)
                    
                    if (importResult != null) {
                        successfulImports++
                    } else {
                        failedImports++
                    }
                } catch (e: Exception) {
                    Log.e("MainDashboardVM", "Error importing file $uri to path: ${_currentPath.value}", e)
                    failedImports++
                }

                // Update progress after each file
                _uiState.update { 
                    it.copy(
                        importProgress = it.importProgress.copy(
                            overallProgress = ((index + 1).toFloat() / uris.size),
                            successfulImports = successfulImports,
                            failedImports = failedImports
                        )
                    ) 
                }

                // Small delay to make progress visible (can be reduced in production)
                kotlinx.coroutines.delay(200)
            }

            // Add final delay to show 100% progress briefly
            kotlinx.coroutines.delay(500)

            // Show success dialog instead of snackbar for successful imports
            if (successfulImports > 0) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        importProgress = ImportProgress(), // Reset progress
                        showImportSuccessDialog = true,
                        lastImportSuccessCount = successfulImports,
                        lastImportFailedCount = failedImports,
                        fileOperationResult = null // Clear snackbar message
                    ) 
                }
            } else {
                // Only show snackbar for complete failures
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        importProgress = ImportProgress(), // Reset progress
                        fileOperationResult = "No files were imported, or all failed."
                    ) 
                }
            }
            navigateToPath(_currentPath.value) // Refresh view
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            Log.e("MainDashboardVM", "Error getting file name from URI: $uri", e)
            null
        }
    }

    fun cancelImport() {
        importJob?.cancel()
        _uiState.update { 
            it.copy(
                isLoading = false,
                importProgress = ImportProgress(isCancelled = true),
                fileOperationResult = "Import cancelled by user."
            ) 
        }
    }

    // Test function to manually trigger import dialog
    fun testImportDialog() {
        Log.d("MainDashboardVM", "Test import dialog triggered")
        _uiState.update { 
            it.copy(
                importProgress = ImportProgress(
                    isImporting = true,
                    totalFiles = 3,
                    currentFileIndex = 1,
                    currentFileName = "test_image.jpg",
                    overallProgress = 0.33f,
                    successfulImports = 0,
                    failedImports = 0,
                    canCancel = true
                )
            ) 
        }
    }

    fun dismissImportSuccessDialog() {
        _uiState.update { 
            it.copy(
                showImportSuccessDialog = false,
                lastImportSuccessCount = 0,
                lastImportFailedCount = 0
            ) 
        }
    }

    fun viewImportedFiles() {
        // Navigate to current path to show imported files
        navigateToPath(_currentPath.value)
    }

    // Test function to show success dialog
    fun testSuccessDialog() {
        _uiState.update { 
            it.copy(
                showImportSuccessDialog = true,
                lastImportSuccessCount = 5,
                lastImportFailedCount = 1
            ) 
        }
    }

    fun cancelUnhide() {
        unhideJob?.cancel()
        _uiState.update { 
            it.copy(
                isLoading = false,
                unhideProgress = UnhideProgress(isCancelled = true),
                fileOperationResult = "Unhide cancelled by user."
            ) 
        }
    }

    fun dismissUnhideSuccessDialog() {
        _uiState.update { 
            it.copy(
                showUnhideSuccessDialog = false,
                lastUnhideSuccessCount = 0,
                lastUnhideFailedCount = 0
            ) 
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
            navigateToPath(_currentPath.value)
        }
    }

    // Selection Mode and Multi-Action Functions
    fun toggleSelection(item: Any) {
        val currentSelection = _uiState.value.selectedItems.toMutableSet()
        if (currentSelection.contains(item)) {
            currentSelection.remove(item)
        } else {
            currentSelection.add(item)
        }
        _uiState.update {
            it.copy(
                selectedItems = currentSelection,
                isSelectionModeActive = currentSelection.isNotEmpty()
            )
        }
    }

    fun enterSelectionMode(item: Any) {
        _uiState.update {
            it.copy(
                isSelectionModeActive = true,
                selectedItems = setOf(item)
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                isSelectionModeActive = false,
                selectedItems = emptySet()
            )
        }
    }

    fun selectAll() {
        val allItems = mutableSetOf<Any>()
        _uiState.value.vaultStats?.let { stats ->
            allItems.addAll(stats.allFolders)
            allItems.addAll(stats.allFiles)
        }
        
        _uiState.update {
            it.copy(
                isSelectionModeActive = allItems.isNotEmpty(),
                selectedItems = allItems
            )
        }
    }

    fun isAllSelected(): Boolean {
        val currentSelection = _uiState.value.selectedItems
        val allItems = mutableSetOf<Any>()
        _uiState.value.vaultStats?.let { stats ->
            allItems.addAll(stats.allFolders)
            allItems.addAll(stats.allFiles)
        }
        return allItems.isNotEmpty() && currentSelection.containsAll(allItems)
    }

    fun requestDeleteSelectedItems() {
        if (_uiState.value.selectedItems.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteConfirmation = true) }
        }
    }

    fun requestUnhideSelectedItems() {
        if (_uiState.value.selectedItems.isNotEmpty()) {
            _uiState.update { it.copy(showUnhideConfirmation = true) }
        }
    }

    fun dismissConfirmationDialogs() {
        _uiState.update { it.copy(showDeleteConfirmation = false, showUnhideConfirmation = false) }
    }

    fun confirmDeleteSelectedItems() {
        val itemsToDelete = _uiState.value.selectedItems
        _uiState.update { it.copy(isLoading = true, showDeleteConfirmation = false) }
        viewModelScope.launch {
            var successCount = 0
            itemsToDelete.forEach { item ->
                val fileToDelete: File? = when (item) {
                    is FileManager.VaultFile -> item.file
                    is FileManager.VaultFolder -> item.folder
                    else -> null
                }
                if (fileToDelete != null && fileManager.deleteFileFromVault(fileToDelete)) {
                    successCount++
                }
            }
            val message = "$successCount/${itemsToDelete.size} items deleted."
            _uiState.update { it.copy(isLoading = false, fileOperationResult = message) }
            clearSelection()
            navigateToPath(_currentPath.value)
        }
    }

    fun confirmUnhideSelectedItems() {
        val itemsToUnhide = _uiState.value.selectedItems.toList()
        if (itemsToUnhide.isEmpty()) return

        // Cancel any existing unhide job
        unhideJob?.cancel()

        Log.d("MainDashboardVM", "Starting unhide progress for ${itemsToUnhide.size} items")
        
        // Initialize unhide progress
        _uiState.update { 
            it.copy(
                isLoading = true, 
                showUnhideConfirmation = false,
                fileOperationResult = null,
                unhideProgress = UnhideProgress(
                    isUnhiding = true,
                    totalFiles = itemsToUnhide.size,
                    canCancel = true
                )
            ) 
        }

        unhideJob = viewModelScope.launch {
            var successCount = 0
            var failedCount = 0

            // Add minimum delay to ensure dialog is visible
            kotlinx.coroutines.delay(500)

            itemsToUnhide.forEachIndexed { index, item ->
                // Check if job was cancelled
                if (!coroutineContext.isActive) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            unhideProgress = UnhideProgress(isCancelled = true),
                            fileOperationResult = "Unhide cancelled by user."
                        ) 
                    }
                    return@launch
                }

                try {
                    // Get item name for progress display
                    val itemName = when (item) {
                        is FileManager.VaultFile -> item.file.name
                        is FileManager.VaultFolder -> item.folder.name
                        else -> "Unknown item"
                    }
                    
                    // Update progress
                    Log.d("MainDashboardVM", "Processing item ${index + 1}/${itemsToUnhide.size}: $itemName")
                    _uiState.update { 
                        it.copy(
                            unhideProgress = it.unhideProgress.copy(
                                currentFileIndex = index + 1,
                                currentFileName = itemName,
                                overallProgress = (index.toFloat() / itemsToUnhide.size),
                                successfulUnhides = successCount,
                                failedUnhides = failedCount
                            )
                        ) 
                    }

                    val success = when (item) {
                        is FileManager.VaultFile -> fileManager.unhideFile(item.file, appContext, FileManager.getUnhideDirectory()) != null
                        is FileManager.VaultFolder -> fileManager.unhideFolderRecursive(item.folder, appContext, FileManager.getUnhideDirectory())
                        else -> false
                    }
                    
                    if (success) {
                        successCount++
                    } else {
                        failedCount++
                    }
                } catch (e: Exception) {
                    Log.e("MainDashboardVM", "Error unhiding item", e)
                    failedCount++
                }

                // Update progress after each item
                _uiState.update { 
                    it.copy(
                        unhideProgress = it.unhideProgress.copy(
                            overallProgress = ((index + 1).toFloat() / itemsToUnhide.size),
                            successfulUnhides = successCount,
                            failedUnhides = failedCount
                        )
                    ) 
                }

                // Small delay to make progress visible
                kotlinx.coroutines.delay(200)
            }

            // Add final delay to show 100% progress briefly
            kotlinx.coroutines.delay(500)

            // Show success dialog for successful unhides
            if (successCount > 0) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        unhideProgress = UnhideProgress(), // Reset progress
                        showUnhideSuccessDialog = true,
                        lastUnhideSuccessCount = successCount,
                        lastUnhideFailedCount = failedCount,
                        fileOperationResult = null // Clear snackbar message
                    ) 
                }
            } else {
                // Only show snackbar for complete failures
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        unhideProgress = UnhideProgress(), // Reset progress
                        fileOperationResult = "No items were unhidden, or all failed."
                    ) 
                }
            }
            
            clearSelection()
            navigateToPath(_currentPath.value) // Refresh view
        }
    }
}
