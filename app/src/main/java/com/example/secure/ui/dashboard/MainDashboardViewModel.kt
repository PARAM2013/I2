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
import kotlinx.coroutines.launch
import java.io.File
import com.example.secure.ui.dashboard.VaultFile
import com.example.secure.ui.dashboard.FileType

class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _vaultFiles = MutableStateFlow<List<VaultFile>>(emptyList())
    val vaultFiles: StateFlow<List<VaultFile>> = _vaultFiles

    private val fileManager = FileManager
    private val context: Context = application.applicationContext

    init {
        loadVaultFiles()
    }

    fun loadVaultFiles() {
        viewModelScope.launch {
            Log.d("MainDashboardViewModel", "Loading vault files...")
            val vaultStats = fileManager.listFilesInVault()

            val combinedList = mutableListOf<VaultFile>()

            // Add folders first
            vaultStats.allFolders.forEach { folder ->
                combinedList.add(VaultFile(
                    name = folder.folder.name,
                    isFolder = true,
                    type = FileType.OTHER // Folders don't have a specific file type
                ))
            }

            // Add files
            vaultStats.allFiles.forEach { fileManagerVaultFile ->
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

            _vaultFiles.value = combinedList
            Log.d("MainDashboardViewModel", "Loaded ${combinedList.size} items (files and folders).")
        }
    }

    fun deleteFile(file: VaultFile) {
        viewModelScope.launch {
            Log.d("MainDashboardViewModel", "Attempting to delete file: ${file.name}")
            val success = fileManager.deleteFileFromVault(File(fileManager.getVaultDirectory(), file.name))
            if (success) {
                Log.d("MainDashboardViewModel", "File deleted successfully: ${file.name}")
                loadVaultFiles() // Refresh list after deletion
            } else {
                Log.e("MainDashboardViewModel", "Failed to delete file: ${file.name}")
            }
        }
    }

    fun unhideFile(file: VaultFile) {
        viewModelScope.launch {
            Log.d("MainDashboardViewModel", "Attempting to unhide file: ${file.name}")
            val unhiddenFile = fileManager.unhideFile(File(fileManager.getVaultDirectory(), file.name), context)
            if (unhiddenFile != null) {
                Log.d("MainDashboardViewModel", "File unhidden successfully: ${file.name} to ${unhiddenFile.absolutePath}")
                loadVaultFiles() // Refresh list after unhide
            } else {
                Log.e("MainDashboardViewModel", "Failed to unhide file: ${file.name}")
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            Log.d("MainDashboardViewModel", "Attempting to create folder: $folderName")
            val newFolder = fileManager.createSubFolderInVault(folderName)
            if (newFolder != null) {
                Log.d("MainDashboardViewModel", "Folder created successfully: ${newFolder.name}")
                loadVaultFiles() // Refresh list after creation
            } else {
                Log.e("MainDashboardViewModel", "Failed to create folder: $folderName")
            }
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            Log.d("MainDashboardViewModel", "Attempting to import file from URI: $uri")
            val importedFile = fileManager.importFile(uri, context)
            if (importedFile != null) {
                Log.d("MainDashboardViewModel", "File imported successfully: ${importedFile.name}")
                loadVaultFiles() // Refresh list after import
            } else {
                Log.e("MainDashboardViewModel", "Failed to import file from URI: $uri")
            }
        }
    }

    // TODO: Implement importFolder if FileManager supports it directly, or break it down into multiple file imports.
}
