package com.example.secure.ui.dashboard

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.secure.R
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.secure.FileImportService
import com.example.secure.file.FileManager
import com.example.secure.file.FileManager.VaultFile
import com.example.secure.util.AppPreferences
import com.example.secure.util.SortManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import java.util.ArrayList
import kotlinx.coroutines.isActive

data class DashboardCategoryItem(
    val id: String,
    val title: String,
    var subtitle: String,
    val iconResId: Int,
    val thumbnail: android.graphics.Bitmap? = null
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

data class MoveProgress(
    val isMoving: Boolean = false,
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val currentFileName: String = "",
    val overallProgress: Float = 0f,
    val successfulMoves: Int = 0,
    val failedMoves: Int = 0,
    val canCancel: Boolean = true,
    val isCancelled: Boolean = false
)

data class MainDashboardUiState(
    val categories: List<DashboardCategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val fileOperationResult: String? = null,
    val showCreateFolderDialog: Boolean = false,
    val vaultStats: FileManager.VaultStats? = null,
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
    val lastUnhideFailedCount: Int = 0,
    val moveProgress: MoveProgress = MoveProgress(),
    val showMoveSuccessDialog: Boolean = false,
    val lastMoveSuccessCount: Int = 0,
    val lastMoveFailedCount: Int = 0,
    val allVaultFolders: List<File> = emptyList()
)

class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainDashboardUiState())
    val uiState: StateFlow<MainDashboardUiState> = _uiState.asStateFlow()

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath.asStateFlow()

    private val fileManager = FileManager
    private val appContext: Context = application.applicationContext
    
    private var unhideJob: Job? = null
    private var moveJob: Job? = null

    private val globalCategoriesTemplate = listOf(
        DashboardCategoryItem("all_files", "All Files", "", R.drawable.ic_folder),
        DashboardCategoryItem("images", "Images", "", R.drawable.ic_image),
        DashboardCategoryItem("videos", "Videos", "", R.drawable.ic_video),
        DashboardCategoryItem("documents", "Documents", "", R.drawable.ic_document)
    )

    private val importProgressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == FileImportService.BROADCAST_ACTION_PROGRESS) {
                val progress = intent.getIntExtra(FileImportService.EXTRA_PROGRESS, 0)
                val total = intent.getIntExtra(FileImportService.EXTRA_TOTAL, 0)
                val fileName = intent.getStringExtra(FileImportService.EXTRA_FILE_NAME) ?: ""
                val finished = intent.getBooleanExtra(FileImportService.EXTRA_FINISHED, false)
                val successCount = intent.getIntExtra(FileImportService.EXTRA_SUCCESS_COUNT, 0)
                val failedCount = intent.getIntExtra(FileImportService.EXTRA_FAILED_COUNT, 0)

                if (finished) {
                    _uiState.update {
                        it.copy(
                            importProgress = ImportProgress(),
                            showImportSuccessDialog = true,
                            lastImportSuccessCount = successCount,
                            lastImportFailedCount = failedCount
                        )
                    }
                    navigateToPath(_currentPath.value)
                } else {
                    _uiState.update {
                        it.copy(
                            importProgress = it.importProgress.copy(
                                isImporting = true,
                                currentFileIndex = progress + 1,
                                totalFiles = total,
                                currentFileName = fileName,
                                overallProgress = (progress.toFloat() / total)
                            )
                        )
                    }
                }
            }
        }
    }

    init {
        _uiState.update { it.copy(sortOption = SortManager.getSortOption(appContext)) }
        loadGlobalDashboardCategories()
        val lastPath = AppPreferences.getLastPath(application)
        navigateToPath(lastPath)
        LocalBroadcastManager.getInstance(appContext).registerReceiver(
            importProgressReceiver,
            IntentFilter(FileImportService.BROADCAST_ACTION_PROGRESS)
        )
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(appContext).unregisterReceiver(importProgressReceiver)
    }

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
                            subtitle = formatCategorySubtitle(videoFiles.size, totalVideoSize)
                        )
                        "documents" -> category.copy(subtitle = formatCategorySubtitle(documentFiles.size, totalDocumentSize))
                        else -> category
                    }
                }
                _uiState.update { it.copy(categories = updatedGlobalCategories, allVaultFolders = allFolders) }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading global category stats", e)
                _uiState.update { oldState ->
                    oldState.copy(categories = globalCategoriesTemplate.map { it.copy(subtitle = "Error") })
                }
            }
        }
    }

    fun loadThumbnailForItem(item: VaultFile) {
        viewModelScope.launch {
            val (thumbnail, duration) = FileManager.generateThumbnailAndDuration(item.file, appContext)
            val updatedItem = item.copy(thumbnail = thumbnail, duration = duration)

            val updateList = { list: List<VaultFile> ->
                list.map { if (it.file.absolutePath == updatedItem.file.absolutePath) updatedItem else it }
            }

            _uiState.update { currentState ->
                currentState.copy(
                    imageFiles = updateList(currentState.imageFiles),
                    videoFiles = updateList(currentState.videoFiles),
                    documentFiles = updateList(currentState.documentFiles),
                    vaultStats = currentState.vaultStats?.let { stats ->
                        stats.copy(allFiles = updateList(stats.allFiles).toMutableList())
                    }
                )
            }
        }
    }

    fun loadAllDocuments() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val allFiles = fileManager.listAllFilesRecursively(FileManager.getVaultDirectory())
                val documentFiles = allFiles.filter { it.category == FileManager.FileCategory.DOCUMENT }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        documentFiles = documentFiles,
                        error = null,
                        allVaultFolders = fileManager.listAllFoldersRecursively(FileManager.getVaultDirectory())
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

    fun loadAllVideos() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val allFiles = fileManager.listAllFilesRecursively(FileManager.getVaultDirectory())
                val videoFiles = allFiles.filter { it.category == FileManager.FileCategory.VIDEO }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        videoFiles = videoFiles,
                        error = null,
                        allVaultFolders = fileManager.listAllFoldersRecursively(FileManager.getVaultDirectory())
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

    private fun loadPathContents(relativePath: String?) {
        if (!fileManager.checkStoragePermissions(appContext)) {
            _uiState.update { it.copy(
                error = "Storage permissions are required to view files. Please grant permissions in Settings.",
                isLoading = false
            ) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
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

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        vaultStats = pathStats.copy(allFiles = sortedFiles),
                        imageFiles = sortedFiles.filter { file -> file.category == FileManager.FileCategory.PHOTO },
                        error = null,
                        allVaultFolders = fileManager.listAllFoldersRecursively(FileManager.getVaultDirectory())
                    )
                }
            } catch (e: Exception) {
                Log.e("MainDashboardVM", "Error loading path contents for $relativePath", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load contents for '$relativePath': ${e.message}",
                        vaultStats = FileManager.VaultStats(),
                        allVaultFolders = emptyList()
                    )
                }
            }
        }
    }

    fun setSortOption(sortOption: SortManager.SortOption) {
        SortManager.saveSortOption(appContext, sortOption)
        _uiState.update { it.copy(sortOption = sortOption) }
        navigateToPath(currentPath.value)
    }

    fun navigateToPath(relativePath: String?) {
        val newPath = if (relativePath.isNullOrBlank() || relativePath == File.separator) null else relativePath
        _currentPath.value = newPath
        AppPreferences.setLastPath(getApplication(), newPath)
        loadPathContents(newPath)
        loadGlobalDashboardCategories()
    }

    fun loadAllImages() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val allFiles = fileManager.listAllFilesRecursively(FileManager.getVaultDirectory())
                val imageFiles = allFiles.filter { it.category == FileManager.FileCategory.PHOTO }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        imageFiles = imageFiles,
                        error = null,
                        allVaultFolders = fileManager.listAllFoldersRecursively(FileManager.getVaultDirectory())
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

        if (!fileManager.checkStoragePermissions(appContext)) {
            _uiState.update { it.copy(
                error = "Storage permissions are required to import files. Please grant permissions in Settings.",
                isLoading = false
            ) }
            return
        }

        _uiState.update { 
            it.copy(
                importProgress = it.importProgress.copy(
                    isImporting = true,
                    totalFiles = uris.size,
                    currentFileIndex = 0,
                    currentFileName = "Starting import..."
                )
            ) 
        }

        val serviceIntent = Intent(appContext, FileImportService::class.java).apply {
            action = FileImportService.ACTION_START_IMPORT
            putParcelableArrayListExtra(FileImportService.EXTRA_FILE_URIS, ArrayList(uris))
            putExtra(FileImportService.EXTRA_SHOW_NOTIFICATION, false) // Don't show notification for in-app import
        }
        appContext.startService(serviceIntent)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
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
        // This is now handled by the service, but we can send a broadcast to cancel if needed.
        // For now, we'll leave it as is, as cancellation was not a primary requirement for the service.
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
        navigateToPath(_currentPath.value)
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

    fun moveFiles(sourceVaultFiles: List<VaultFile>, destinationDirectory: File) {
        if (sourceVaultFiles.isEmpty()) return

        moveJob?.cancel()

        _uiState.update { 
            it.copy(
                isLoading = true, 
                fileOperationResult = null,
                moveProgress = MoveProgress(
                    isMoving = true,
                    totalFiles = sourceVaultFiles.size,
                    canCancel = true
                )
            ) 
        }

        moveJob = viewModelScope.launch {
            var successfulMoves = 0
            var failedMoves = 0

            delay(500)

            sourceVaultFiles.forEachIndexed { index, vaultFile ->
                if (!isActive) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            moveProgress = MoveProgress(isCancelled = true),
                            fileOperationResult = "Move operation cancelled by user."
                        ) 
                    }
                    return@launch
                }

                try {
                    val fileName = vaultFile.file.name
                    _uiState.update { 
                        it.copy(
                            moveProgress = it.moveProgress.copy(
                                currentFileIndex = index + 1,
                                currentFileName = fileName,
                                overallProgress = (index.toFloat() / sourceVaultFiles.size),
                                successfulMoves = successfulMoves,
                                failedMoves = failedMoves
                            )
                        ) 
                    }

                    val movedFile = fileManager.moveFileInVault(vaultFile.file, destinationDirectory)
                    if (movedFile != null) {
                        successfulMoves++
                    } else {
                        failedMoves++
                    }
                } catch (e: Exception) {
                    Log.e("MainDashboardVM", "Error moving file ${vaultFile.file.name}", e)
                    failedMoves++
                }

                _uiState.update { 
                    it.copy(
                        moveProgress = it.moveProgress.copy(
                            overallProgress = ((index + 1).toFloat() / sourceVaultFiles.size),
                            successfulMoves = successfulMoves,
                            failedMoves = failedMoves
                        )
                    ) 
                }
                delay(200)
            }

            delay(500)

            if (successfulMoves > 0) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        moveProgress = MoveProgress(),
                        showMoveSuccessDialog = true,
                        lastMoveSuccessCount = successfulMoves,
                        lastMoveFailedCount = failedMoves,
                        fileOperationResult = null
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        moveProgress = MoveProgress(),
                        fileOperationResult = "No files were moved, or all failed."
                    ) 
                }
            }
            clearSelection()
            navigateToPath(_currentPath.value)
        }
    }

    fun cancelMove() {
        moveJob?.cancel()
        _uiState.update { 
            it.copy(
                isLoading = false,
                moveProgress = MoveProgress(isCancelled = true),
                fileOperationResult = "Move operation cancelled by user."
            ) 
        }
    }

    fun dismissMoveSuccessDialog() {
        _uiState.update { 
            it.copy(
                showMoveSuccessDialog = false,
                lastMoveSuccessCount = 0,
                lastMoveFailedCount = 0
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
                    navigateToPath(_currentPath.value)
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
                operationMessage = appContext.getString(R.string.folder_name_empty_error)
            } else if (newName == oldName) {
                operationMessage = "New name is the same as the old name."
            }
            else {
                val renamedFile = fileManager.renameItemInVault(fileToRename, newName, appContext)
                if (renamedFile != null) {
                    operationMessage = appContext.getString(R.string.rename_success, oldName, newName)
                } else {
                    val newFileCheck = File(fileToRename.parentFile, newName)
                    if (newFileCheck.exists()){
                        operationMessage = appContext.getString(R.string.rename_failed_exists, newName)
                    } else if (newName.contains(File.separatorChar) || newName == "." || newName == ".." || newName.any { it in "\\:*?\"<>|" }) {
                        operationMessage = appContext.getString(R.string.error_invalid_name, newName)
                    }
                    else {
                        operationMessage = appContext.getString(R.string.rename_failed, oldName)
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false, fileOperationResult = operationMessage) }
            navigateToPath(_currentPath.value)
        }
    }

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

        unhideJob?.cancel()

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

            delay(500)

            itemsToUnhide.forEachIndexed { index, item ->
                if (!isActive) {
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
                    val itemName = when (item) {
                        is FileManager.VaultFile -> item.file.name
                        is FileManager.VaultFolder -> item.folder.name
                        else -> "Unknown item"
                    }
                    
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

                _uiState.update { 
                    it.copy(
                        unhideProgress = it.unhideProgress.copy(
                            overallProgress = ((index + 1).toFloat() / itemsToUnhide.size),
                            successfulUnhides = successCount,
                            failedUnhides = failedCount
                        )
                    ) 
                }
                delay(200)
            }

            delay(500)

            if (successCount > 0) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        unhideProgress = UnhideProgress(),
                        showUnhideSuccessDialog = true,
                        lastUnhideSuccessCount = successCount,
                        lastUnhideFailedCount = failedCount,
                        fileOperationResult = null
                    ) 
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        unhideProgress = UnhideProgress(),
                        fileOperationResult = "No items were unhidden, or all failed."
                    ) 
                }
            }
            
            clearSelection()
            navigateToPath(_currentPath.value)
        }
    }
}
