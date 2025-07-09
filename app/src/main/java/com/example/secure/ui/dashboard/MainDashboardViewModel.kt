package com.example.secure.ui.dashboard

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secure.file.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import com.example.secure.ui.dashboard.VaultFile
import com.example.secure.ui.dashboard.FileType

class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _vaultFiles = MutableStateFlow<List<VaultFile>>(emptyList())
    val vaultFiles: StateFlow<List<VaultFile>> = _vaultFiles.asStateFlow()

    private val _currentPath = MutableStateFlow("") // Root path is empty string
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val fileManager = FileManager
    private val context: Context = application.applicationContext

    init {
        loadVaultFiles()
    }

    fun loadVaultFiles() {
        viewModelScope.launch {
            val path = _currentPath.value
            Log.d("MainDashboardViewModel", "Loading vault files for path: '$path'")
            val currentDirectory = if (path.isEmpty()) {
                fileManager.getVaultDirectory()
            } else {
                File(fileManager.getVaultDirectory(), path)
            }

            // This will be adjusted in Step 2 to correctly list immediate children.
            // For now, it will list recursively, and we'll rely on UI to handle navigation.
            val vaultStats = fileManager.listFilesInVault(currentDirectory)

            val combinedList = mutableListOf<VaultFile>()

            // Add folders first from the current directory
            // Assuming listFilesInVault will be modified to list direct children,
            // or we will filter here. For now, this structure anticipates that.
            vaultStats.allFolders.filter {
                // If listFilesInVault is not modified, this filter is crucial.
                // It ensures we only show folders directly under currentDirectory.
                it.folder.parentFile?.absolutePath == currentDirectory.absolutePath || (currentDirectory == fileManager.getVaultDirectory() && it.folder.parentFile?.absolutePath == fileManager.getVaultDirectory().absolutePath)
            }.forEach { folder ->
                combinedList.add(VaultFile(
                    name = folder.folder.name,
                    isFolder = true,
                    type = FileType.FOLDER // Explicitly FOLDER
                ))
            }

            // Add files from the current directory
            vaultStats.allFiles.filter {
                // Similar filter for files.
                it.file.parentFile?.absolutePath == currentDirectory.absolutePath
            }.forEach { fileManagerVaultFile ->
                combinedList.add(VaultFile(
                    name = fileManagerVaultFile.file.name,
                    isFolder = false,
                    type = when (fileManagerVaultFile.category) {
                        FileManager.FileCategory.PHOTO -> FileType.IMAGE
                        FileManager.FileCategory.VIDEO -> FileType.VIDEO
                        FileManager.FileCategory.DOCUMENT -> FileType.DOCUMENT
                        FileManager.FileCategory.OTHER -> FileType.OTHER
                    }
                ))
            }
            // Sort: folders first, then by name
            combinedList.sortWith(compareByDescending<VaultFile> { it.isFolder }.thenBy { it.name })


            _vaultFiles.value = combinedList
            Log.d("MainDashboardViewModel", "Loaded ${combinedList.size} items for path '$path'.")
        }
    }

    fun navigateTo(folderName: String) {
        val newPath = if (_currentPath.value.isEmpty()) {
            folderName
        } else {
            _currentPath.value + File.separator + folderName
        }
        _currentPath.value = newPath
        loadVaultFiles()
    }

    fun navigateUp() {
        val current = _currentPath.value
        if (current.isNotEmpty()) {
            val lastSeparator = current.lastIndexOf(File.separator)
            _currentPath.value = if (lastSeparator > -1) {
                current.substring(0, lastSeparator)
            } else {
                "" // Navigate to root
            }
            loadVaultFiles()
        }
    }


    fun deleteFile(file: VaultFile) {
        viewModelScope.launch {
            val targetPath = if (_currentPath.value.isEmpty()) file.name else _currentPath.value + File.separator + file.name
            Log.d("MainDashboardViewModel", "Attempting to delete item: $targetPath")
            val success = fileManager.deleteFileFromVault(File(fileManager.getVaultDirectory(), targetPath))
            if (success) {
                Log.d("MainDashboardViewModel", "Item deleted successfully: $targetPath")
                loadVaultFiles() // Refresh list after deletion
            } else {
                Log.e("MainDashboardViewModel", "Failed to delete item: $targetPath")
            }
        }
    }

    fun unhideFile(file: VaultFile) {
        viewModelScope.launch {
            if (file.isFolder) {
                Log.w("MainDashboardViewModel", "Unhide operation is not supported for folders.")
                // Optionally show a message to the user
                return@launch
            }
            val targetPath = if (_currentPath.value.isEmpty()) file.name else _currentPath.value + File.separator + file.name
            Log.d("MainDashboardViewModel", "Attempting to unhide file: $targetPath")
            val unhiddenFile = fileManager.unhideFile(File(fileManager.getVaultDirectory(), targetPath), context)
            if (unhiddenFile != null) {
                Log.d("MainDashboardViewModel", "File unhidden successfully: $targetPath to ${unhiddenFile.absolutePath}")
                loadVaultFiles() // Refresh list after unhide
            } else {
                Log.e("MainDashboardViewModel", "Failed to unhide file: $targetPath")
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            Log.d("MainDashboardViewModel", "Attempting to create folder: $folderName in path: ${_currentPath.value}")
            val newFolder = fileManager.createSubFolderInVault(folderName, _currentPath.value.ifEmpty { null })
            if (newFolder != null) {
                Log.d("MainDashboardViewModel", "Folder created successfully: ${newFolder.name} in ${_currentPath.value}")
                loadVaultFiles() // Refresh list after creation
            } else {
                Log.e("MainDashboardViewModel", "Failed to create folder: $folderName in ${_currentPath.value}")
            }
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            Log.d("MainDashboardViewModel", "Attempting to import file from URI: $uri into path: ${_currentPath.value}")
            val importedFile = fileManager.importFile(uri, context, _currentPath.value.ifEmpty { null })
            if (importedFile != null) {
                Log.d("MainDashboardViewModel", "File imported successfully: ${importedFile.name} into ${_currentPath.value}")
                loadVaultFiles() // Refresh list after import
            } else {
                Log.e("MainDashboardViewModel", "Failed to import file from URI: $uri into ${_currentPath.value}")
            }
        }
    }

    // TODO: Implement importFolder if FileManager supports it directly, or break it down into multiple file imports.
}
