package com.example.secure.file

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

object FileManager {

    const val VAULT_FOLDER_NAME = ".iSecureVault" // Hidden folder
    const val UNHIDE_FOLDER_NAME = "SecureUnhide" // In Downloads for unhidden files

    // Request code for storage permissions
    const val REQUEST_STORAGE_PERMISSION_CODE = 101
    const val REQUEST_MANAGE_STORAGE_PERMISSION_CODE = 102 // For Android 11+

    /**
     * Gets the root directory for the iSecure vault.
     * This will be in public external storage to persist after uninstall.
     * Example: /storage/emulated/0/.iSecureVault
     */
    fun getVaultDirectory(): File {
        // Using getExternalStorageDirectory() for broader access, ensure permissions are handled.
        // For API 29+, this path might be restricted by Scoped Storage for direct access without SAF or MANAGE_EXTERNAL_STORAGE.
        // However, files created by the app in public directories (even custom ones) before Scoped Storage enforcement,
        // or by previous versions of the app, might still be accessible.
        // The requirement for persistence after uninstall points to a public, non-app-specific directory.
        val publicRootDir = Environment.getExternalStorageDirectory()
        val vaultDir = File(publicRootDir, VAULT_FOLDER_NAME)
        if (!vaultDir.exists()) {
            Log.d("FileManager", "Creating vault directory: ${vaultDir.absolutePath}")
            vaultDir.mkdirs() // Attempt to create if it doesn't exist (requires permission)
        }
        Log.d("FileManager", "Vault directory: ${vaultDir.absolutePath}")
        return vaultDir
    }

    /**
     * Gets the directory where files are restored/unhidden.
     * Example: /storage/emulated/0/Download/SecureUnhide
     */
    fun getUnhideDirectory(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val unhideDir = File(downloadsDir, UNHIDE_FOLDER_NAME)
        if (!unhideDir.exists()) {
            Log.d("FileManager", "Creating unhide directory: ${unhideDir.absolutePath}")
            unhideDir.mkdirs()
        }
        Log.d("FileManager", "Unhide directory: ${unhideDir.absolutePath}")
        return unhideDir
    }


    fun checkStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 (API 23) to Android 10 (API 29)
            val readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 6 (API 23), permissions are granted at install time
            true
        }
    }

    fun requestStoragePermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above - Request MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = android.net.Uri.parse("package:${activity.packageName}")
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback or error handling if the intent fails (e.g., on some custom ROMs or emulators)
                    android.util.Log.e("FileManager", "Failed to launch MANAGE_EXTERNAL_STORAGE settings", e)
                    // As a fallback, request legacy permissions, though they won't grant full access for the vault's purpose on API 30+
                    requestLegacyStoragePermissions(activity)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 (API 23) to Android 10 (API 29) - Request legacy permissions
            requestLegacyStoragePermissions(activity)
        }
        // For versions below M, permissions are granted at install time, so no runtime request needed.
    }

    private fun requestLegacyStoragePermissions(activity: Activity) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        // WRITE_EXTERNAL_STORAGE is effectively needed up to API 28 for creating/modifying files outside app-specific dirs.
        // While API 29+ gives it implicitly with READ for app-specific dirs, our vault is public.
        // However, for API 29, write access to public directories is restricted by Scoped Storage.
        // This logic primarily serves pre-API 29 or as a fallback.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On API 29, WRITE_EXTERNAL_STORAGE is not needed for app to write to its own public directories if it opts out of scoped storage (legacy).
            // But for creating a new public directory like .iSecureVault, it's complex.
            // For simplicity, we'll only request READ here for API 29 if WRITE is not already granted,
            // acknowledging that full public write access is more nuanced.
            // The MANAGE_EXTERNAL_STORAGE path is clearer for API 30+.
            // The app will need to handle failures gracefully if it can't write.
        }


        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), REQUEST_STORAGE_PERMISSION_CODE)
        }
    }

    // TODO: Add methods for file operations like:
    // fun importFile(sourceFile: File, context: Context): File? (moves file to vault)
    // fun unhideFile(vaultFile: File, context: Context): File? (moves file from vault to unhide dir)
    // fun deleteFileFromVault(fileInVault: File): Boolean
    // fun listFilesInVault(subDirectory: String? = null): List<File> -> Implemented with VaultStats
    // fun createSubFolderInVault(folderName: String): File? -> Implemented

    fun deleteFileFromVault(item: File): Boolean {
        if (!item.path.startsWith(getVaultDirectory().path)) {
            Log.w("FileManager", "Attempt to delete item outside vault: ${item.path}")
            return false
        }
        if (item.exists()) {
            val deleted = if (item.isDirectory) {
                item.deleteRecursively() // Deletes folder and its contents
            } else {
                item.delete() // Deletes a single file
            }
            if (deleted) {
                Log.d("FileManager", "Successfully deleted: ${item.path}")
            } else {
                Log.e("FileManager", "Failed to delete: ${item.path}")
            }
            return deleted
        }
        Log.w("FileManager", "Attempt to delete non-existent item: ${item.path}")
        return false // Item doesn't exist
    }

    fun unhideFile(vaultFile: File, context: Context): File? {
        if (!vaultFile.exists() || !vaultFile.isFile) {
            Log.w("FileManager", "Unhide failed: source file does not exist or is not a file: ${vaultFile.path}")
            return null
        }
        if (!vaultFile.path.startsWith(getVaultDirectory().path)) {
            Log.w("FileManager", "Attempt to unhide file outside vault: ${vaultFile.path}")
            return null
        }

        val unhideDir = getUnhideDirectory()
        if (!unhideDir.exists() && !unhideDir.mkdirs()) {
            Log.e("FileManager", "Failed to create unhide directory: ${unhideDir.path}")
            return null
        }

        var destinationFile = File(unhideDir, vaultFile.name)
        var counter = 1
        val nameWithoutExt = vaultFile.nameWithoutExtension
        val extension = vaultFile.extension

        // Handle file name conflicts in the unhide directory
        while (destinationFile.exists()) {
            val newName = if (extension.isEmpty()) {
                "$nameWithoutExt (${counter++})"
            } else {
                "$nameWithoutExt (${counter++}).$extension"
            }
            destinationFile = File(unhideDir, newName)
        }

        try {
            Log.d("FileManager", "Attempting to unhide ${vaultFile.name} to ${destinationFile.absolutePath}")
            if (vaultFile.renameTo(destinationFile)) {
                Log.d("FileManager", "Successfully unhidden ${vaultFile.name} via rename.")
                // Optionally, trigger a media scan for the unhidden file so it appears in galleries etc.
                // This is where 'context' would be useful.
                // MediaScannerConnection.scanFile(context, arrayOf(destinationFile.absolutePath), null, null)
                return destinationFile
            } else {
                Log.w("FileManager", "Rename failed for ${vaultFile.name}, attempting copy and delete.")
                // Fallback: try copy and delete if rename fails
                vaultFile.copyTo(destinationFile, overwrite = true) // overwrite should be safe due to conflict handling
                if (destinationFile.exists() && destinationFile.length() == vaultFile.length()) {
                    vaultFile.delete()
                    Log.d("FileManager", "Successfully unhidden ${vaultFile.name} via copy/delete.")
                    // MediaScannerConnection.scanFile(context, arrayOf(destinationFile.absolutePath), null, null)
                    return destinationFile
                } else {
                    Log.e("FileManager", "Failed to move file to unhide directory after copy attempt: ${vaultFile.name}")
                    if(destinationFile.exists()) destinationFile.delete() // Clean up partial copy
                    return null
                }
            }
        } catch (e: Exception) {
            Log.e("FileManager", "Error unhiding file: ${vaultFile.name}", e)
            e.printStackTrace()
            return null
        }
    }

    // Enum for file categories
    enum class FileCategory {
        PHOTO, VIDEO, DOCUMENT, OTHER
    }

    // Data class to hold file details
    data class VaultFile(
        val file: File,
        val category: FileCategory,
        val size: Long
    )

    // Data class to hold folder details
    data class VaultFolder(
        val folder: File,
        var totalFiles: Int = 0,
        var totalSize: Long = 0,
        val subFolders: MutableList<VaultFolder> = mutableListOf(),
        val files: MutableList<VaultFile> = mutableListOf()
    )

    // Data class to hold overall vault statistics
    data class VaultStats(
        val allFiles: MutableList<VaultFile> = mutableListOf(),
        val allFolders: MutableList<VaultFolder> = mutableListOf(),
        var totalPhotoFiles: Int = 0,
        var totalPhotoSize: Long = 0,
        var totalVideoFiles: Int = 0,
        var totalVideoSize: Long = 0,
        var totalDocumentFiles: Int = 0,
        var totalDocumentSize: Long = 0,
        var totalOtherFiles: Int = 0,
        var totalOtherSize: Long = 0,
        var grandTotalFiles: Int = 0,
        var grandTotalSize: Long = 0,
        var grandTotalFolders: Int = 0
    )

    fun getFileCategory(fileName: String): FileCategory {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> FileCategory.PHOTO
            "mp4", "mkv", "avi", "mov", "wmv", "flv" -> FileCategory.VIDEO
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf" -> FileCategory.DOCUMENT
            else -> FileCategory.OTHER
        }
    }

    fun listFilesInVault(directory: File = getVaultDirectory()): VaultStats {
        val stats = VaultStats()
        val items = directory.listFiles() ?: return stats // Return empty stats if directory is not listable

        for (item in items) {
            if (item.isDirectory) {
                stats.grandTotalFolders++ // Counts as one folder in the current listing
                val folderStat = VaultFolder(folder = item, totalFiles = 0, totalSize = 0) // Initialize with 0, not recursive content
                // We list immediate children, so subFolders and files within this VaultFolder are not populated here.
                // The UI will navigate into it and call listFilesInVault again for that sub-folder.
                stats.allFolders.add(folderStat)
            } else { // It's a file
                val category = getFileCategory(item.name)
                val size = item.length()
                val vaultFile = VaultFile(item, category, size)

                stats.allFiles.add(vaultFile) // Add to list of files in current directory
                stats.grandTotalFiles++
                stats.grandTotalSize += size

                // Update category-specific stats for the current directory's content
                when (category) {
                    FileCategory.PHOTO -> {
                        stats.totalPhotoFiles++
                        stats.totalPhotoSize += size
                    }
                    FileCategory.VIDEO -> {
                        stats.totalVideoFiles++
                        stats.totalVideoSize += size
                    }
                    FileCategory.DOCUMENT -> {
                        stats.totalDocumentFiles++
                        stats.totalDocumentSize += size
                    }
                    FileCategory.OTHER -> {
                        stats.totalOtherFiles++
                        stats.totalOtherSize += size
                    }
                }
            }
        }
        // Sort folders and files by name for consistent listing (optional, can also be done in ViewModel)
        stats.allFolders.sortBy { it.folder.name }
        stats.allFiles.sortBy { it.file.name }

        return stats
    }

    fun createSubFolderInVault(folderName: String, parentRelativePath: String? = null): File? {
        if (folderName.isBlank() || folderName.contains(File.separatorChar)) {
            Log.w("FileManager", "Invalid folder name: $folderName")
            return null
        }

        val parentDir = if (parentRelativePath.isNullOrBlank()) {
            getVaultDirectory()
        } else {
            File(getVaultDirectory(), parentRelativePath)
        }

        if (!parentDir.exists() || !parentDir.isDirectory) {
            Log.e("FileManager", "Parent directory does not exist or is not a directory: ${parentDir.absolutePath}")
            return null
        }

        val newFolder = File(parentDir, folderName)
        return if (newFolder.exists()) {
            if (newFolder.isDirectory) {
                Log.d("FileManager", "createSubFolderInVault: Folder already exists: ${newFolder.absolutePath}")
                newFolder
            } else {
                Log.e("FileManager", "createSubFolderInVault: Cannot create folder, a file with the same name exists: ${newFolder.absolutePath}")
                null // Exists and is a file, return null.
            }
        } else {
            try {
                if (newFolder.mkdirs()) {
                    Log.d("FileManager", "createSubFolderInVault: Folder created successfully: ${newFolder.absolutePath}")
                    newFolder
                } else {
                    Log.e("FileManager", "createSubFolderInVault: Failed to create folder (mkdirs returned false): ${newFolder.absolutePath}")
                    null // Try to create, return if successful
                }
            } catch (e: SecurityException) {
                Log.e("FileManager", "createSubFolderInVault: SecurityException creating folder ${newFolder.absolutePath}: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e("FileManager", "createSubFolderInVault: Unexpected error creating folder ${newFolder.absolutePath}: ${e.message}")
                null
            }
        }
    }

    fun importFile(sourceFileUri: android.net.Uri, context: Context, targetRelativePath: String? = null, deleteOriginal: Boolean = true): File? {
        val contentResolver = context.contentResolver
        var fileName: String? = null
        var fileSize: Long = 0

        // Get file name and size from URI
        contentResolver.query(sourceFileUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }

        if (fileName == null) {
            Log.e("FileManager", "importFile: Could not determine file name from URI: $sourceFileUri")
            return null
        }
        if (fileName!!.contains(File.separatorChar) || fileName == "." || fileName == "..") {
            Log.e("FileManager", "importFile: Invalid filename extracted from URI: $fileName")
            return null
        }


        val targetDir = if (targetRelativePath.isNullOrBlank()) {
            getVaultDirectory()
        } else {
            File(getVaultDirectory(), targetRelativePath)
        }

        if (!targetDir.exists()) {
            Log.d("FileManager", "importFile: Target directory does not exist, attempting to create: ${targetDir.absolutePath}")
            if (!targetDir.mkdirs()) {
                Log.e("FileManager", "importFile: Failed to create target directory in vault: ${targetDir.absolutePath}")
                return null
            }
        }
        if(!targetDir.isDirectory){
            Log.e("FileManager", "importFile: Target path is not a directory: ${targetDir.absolutePath}")
            return null
        }


        var destinationFile = File(targetDir, fileName!!)
        var counter = 1
        val nameWithoutExt = fileName!!.substringBeforeLast('.')
        val extension = fileName!!.substringAfterLast('.', "")

        // Handle file name conflicts
        while (destinationFile.exists()) {
            val newName = if (extension.isEmpty()) {
                "$nameWithoutExt (${counter++})"
            } else {
                "$nameWithoutExt (${counter++}).$extension"
            }
            destinationFile = File(targetDir, newName)
        }

        try {
            Log.d("FileManager", "importFile: Importing file from $sourceFileUri to ${destinationFile.absolutePath}")
            contentResolver.openInputStream(sourceFileUri)?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run { Log.e("FileManager", "importFile: Failed to open input stream for URI: $sourceFileUri"); return null } // Failed to open input stream

            Log.d("FileManager", "importFile: File copied successfully to ${destinationFile.absolutePath}")

            // Optionally delete original file
            if (deleteOriginal) {
                if ("file" == sourceFileUri.scheme) {
                    sourceFileUri.path?.let { path ->
                        val originalFile = File(path)
                        if (originalFile.exists() && originalFile.delete()) {
                            Log.d("FileManager", "importFile: Successfully deleted original file (file:// scheme): $sourceFileUri")
                        } else {
                            Log.w("FileManager", "importFile: Failed to delete original file (file:// scheme) or file did not exist: $sourceFileUri")
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                           android.provider.DocumentsContract.isDocumentUri(context, sourceFileUri)) {
                    try {
                        if (android.provider.DocumentsContract.deleteDocument(context.contentResolver, sourceFileUri)) {
                            Log.i("FileManager", "importFile: Original document deleted successfully: $sourceFileUri")
                        } else {
                            Log.w("FileManager", "importFile: Failed to delete original document (deleteDocument returned false): $sourceFileUri")
                        }
                    } catch (e: SecurityException) {
                        Log.e("FileManager", "importFile: SecurityException when trying to delete original document: $sourceFileUri", e)
                    } catch (e: Exception) {
                        Log.e("FileManager", "importFile: Exception when trying to delete original document: $sourceFileUri", e)
                    }
                } else {
                     Log.w("FileManager", "importFile: Original file at $sourceFileUri was not a file:// URI or a deletable Document URI. Manual deletion might be required by user.")
                }
            }

            return destinationFile
        } catch (e: Exception) {
            Log.e("FileManager", "importFile: Error importing file from URI: $sourceFileUri", e)
            e.printStackTrace()
            // If copy failed, delete the partially created file
            if (destinationFile.exists()) {
                Log.w("FileManager", "importFile: Deleting partially created file: ${destinationFile.absolutePath}")
                destinationFile.delete()
            }
            return null
        }
    }
}
