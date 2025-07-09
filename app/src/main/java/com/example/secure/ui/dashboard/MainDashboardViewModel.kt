package com.example.secure.ui.dashboard

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secure.file.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

// Using the VaultFile data class from FileListItem.kt for UI representation
// Mapping from FileManager.VaultFile to this VaultFile will be needed

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
            val vaultStats = fileManager.listFilesInVault()
            // Map FileManager.VaultFile to ui.dashboard.VaultFile
            _vaultFiles.value = vaultStats.allFiles.map { fileManagerVaultFile ->
                VaultFile(
                    name = fileManagerVaultFile.file.name,
                    isFolder = fileManagerVaultFile.file.isDirectory,
                    type = when (fileManagerVaultFile.category) {
                        FileManager.FileCategory.PHOTO -> FileType.IMAGE
                        FileManager.FileCategory.VIDEO -> FileType.VIDEO
                        FileManager.FileCategory.DOCUMENT -> FileType.DOCUMENT
                        FileManager.FileCategory.OTHER -> FileType.OTHER
                    }
                )
            }
        }
    }

    fun deleteFile(file: VaultFile) {
        viewModelScope.launch {
            val success = fileManager.deleteFileFromVault(File(fileManager.getVaultDirectory(), file.name))
            if (success) {
                loadVaultFiles() // Refresh list after deletion
            } else {
                // Handle error (e.g., show a toast)
            }
        }
    }

    fun unhideFile(file: VaultFile) {
        viewModelScope.launch {
            val success = fileManager.unhideFile(File(fileManager.getVaultDirectory(), file.name), context)
            if (success != null) {
                loadVaultFiles() // Refresh list after unhide
            } else {
                // Handle error
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            val newFolder = fileManager.createSubFolderInVault(folderName)
            if (newFolder != null) {
                loadVaultFiles() // Refresh list after creation
            } else {
                // Handle error
            }
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            val importedFile = fileManager.importFile(uri, context)
            if (importedFile != null) {
                loadVaultFiles() // Refresh list after import
            } else {
                // Handle error
            }
        }
    }

    // TODO: Implement importFolder if FileManager supports it directly, or break it down into multiple file imports.
}
