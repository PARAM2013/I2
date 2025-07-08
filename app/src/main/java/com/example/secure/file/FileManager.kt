package com.example.secure.file

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
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
            vaultDir.mkdirs() // Attempt to create if it doesn't exist (requires permission)
        }
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
            unhideDir.mkdirs()
        }
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
                    activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE_PERMISSION_CODE)
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
            // Security check: ensure the file or folder is actually within the vault
            android.util.Log.w("FileManager", "Attempt to delete item outside vault: ${item.path}")
            return false
        }
        if (item.exists()) {
            return if (item.isDirectory) {
                item.deleteRecursively() // Deletes folder and its contents
            } else {
                item.delete() // Deletes a single file
            }
        }
        return false // Item doesn't exist
    }

    fun unhideFile(vaultFile: File, context: Context): File? {
        if (!vaultFile.exists() || !vaultFile.isFile) {
            android.util.Log.w("FileManager", "Unhide failed: source file does not exist or is not a file: ${vaultFile.path}")
            return null
        }
        if (!vaultFile.path.startsWith(getVaultDirectory().path)) {
            android.util.Log.w("FileManager", "Attempt to unhide file outside vault: ${vaultFile.path}")
            return null
        }

        val unhideDir = getUnhideDirectory()
        if (!unhideDir.exists() && !unhideDir.mkdirs()) {
            android.util.Log.e("FileManager", "Failed to create unhide directory: ${unhideDir.path}")
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
            // Using renameTo for moving files within the same filesystem is generally efficient.
            // If it fails (e.g., across different filesystems, though less likely here),
            // a copy-then-delete strategy would be a fallback.
            if (vaultFile.renameTo(destinationFile)) {
                // Optionally, trigger a media scan for the unhidden file so it appears in galleries etc.
                // This is where 'context' would be useful.
                // MediaScannerConnection.scanFile(context, arrayOf(destinationFile.absolutePath), null, null)
                return destinationFile
            } else {
                // Fallback: try copy and delete if rename fails
                vaultFile.copyTo(destinationFile, overwrite = true) // overwrite should be safe due to conflict handling
                if (destinationFile.exists() && destinationFile.length() == vaultFile.length()) {
                    vaultFile.delete()
                    // MediaScannerConnection.scanFile(context, arrayOf(destinationFile.absolutePath), null, null)
                    return destinationFile
                } else {
                    android.util.Log.e("FileManager", "Failed to move file to unhide directory after copy attempt: ${vaultFile.name}")
                    if(destinationFile.exists()) destinationFile.delete() // Clean up partial copy
                    return null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FileManager", "Error unhiding file: ${vaultFile.name}", e)
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

        fun processDirectory(currentDir: File, parentFolderStat: VaultFolder?) {
            val items = currentDir.listFiles() ?: return

            if (parentFolderStat == null) { // Root directory processing
                // Correctly count only top-level folders for stats.grandTotalFolders initially
                // Sub-folder counts will be part of their parent VaultFolder objects if needed elsewhere
                 stats.grandTotalFolders = items.count { it.isDirectory }
            }


            for (item in items) {
                if (item.isDirectory) {
                    val folderStat = VaultFolder(item)
                    if (parentFolderStat == null) { // Root level folder
                        stats.allFolders.add(folderStat)
                    } else {
                        parentFolderStat.subFolders.add(folderStat)
                    }
                    processDirectory(item, folderStat) // Recurse for subdirectories

                    // Accumulate file counts and sizes from subdirectories into their direct parent
                    if (parentFolderStat != null) {
                        parentFolderStat.totalFiles += folderStat.totalFiles
                        parentFolderStat.totalSize += folderStat.totalSize
                    } else {
                        // For top-level folders, their own totalFiles and totalSize are calculated within processDirectory
                        // The grand total for files and size is accumulated directly from files discovered at any level
                    }
                } else { // It's a file
                    val category = getFileCategory(item.name)
                    val size = item.length()
                    val vaultFile = VaultFile(item, category, size)

                    stats.allFiles.add(vaultFile) // Add to flat list of all files
                    stats.grandTotalFiles++
                    stats.grandTotalSize += size

                    if (parentFolderStat != null) {
                        parentFolderStat.files.add(vaultFile)
                        parentFolderStat.totalFiles++
                        parentFolderStat.totalSize += size
                    }
                    // If parentFolderStat is null, it means the file is in the root of the scanned directory.
                    // These files contribute to grandTotalFiles and grandTotalSize directly.

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
        }
        processDirectory(directory, null)
        return stats
    }

    fun createSubFolderInVault(folderName: String, parentRelativePath: String? = null): File? {
        if (folderName.isBlank() || folderName.contains(File.separatorChar)) {
            // Invalid folder name
            return null
        }

        val parentDir = if (parentRelativePath.isNullOrBlank()) {
            getVaultDirectory()
        } else {
            File(getVaultDirectory(), parentRelativePath)
        }

        if (!parentDir.exists() || !parentDir.isDirectory) {
            // Parent directory doesn't exist or is not a directory
            return null
        }

        val newFolder = File(parentDir, folderName)
        return if (newFolder.exists()) {
            if (newFolder.isDirectory) newFolder else null // Exists and is a directory, return it. If it's a file, return null.
        } else {
            if (newFolder.mkdirs()) newFolder else null // Try to create, return if successful
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
            // Could not determine file name from URI
            return null
        }
        if (fileName!!.contains(File.separatorChar) || fileName == "." || fileName == "..") {
             // Invalid filename
            return null
        }


        val targetDir = if (targetRelativePath.isNullOrBlank()) {
            getVaultDirectory()
        } else {
            File(getVaultDirectory(), targetRelativePath)
        }

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            // Failed to create target directory in vault
            return null
        }
        if(!targetDir.isDirectory){
            //Target path is not a directory
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
            contentResolver.openInputStream(sourceFileUri)?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null // Failed to open input stream

            // Optionally delete original file
            // This is tricky with Uris, especially for files not owned by the app or from external providers.
            // For SAF Uris (content://), direct deletion is usually done via DocumentsContract.deleteDocument.
            // For file:// Uris (if we ever get those directly, less common now), direct File.delete() might work if we have path.
            // Given the complexity and risk, especially with Scoped Storage, we'll make this robust later.
            // For now, if deleteOriginal is true, we'll attempt it only if it's a file URI and we can get a path.
            // This part needs significant improvement for production.
            if (deleteOriginal) {
                if ("file" == sourceFileUri.scheme) {
                    sourceFileUri.path?.let { File(it).delete() }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                           android.provider.DocumentsContract.isDocumentUri(context, sourceFileUri)) {
                    try {
                        if (android.provider.DocumentsContract.deleteDocument(context.contentResolver, sourceFileUri)) {
                            android.util.Log.i("FileManager", "Original document deleted successfully: $sourceFileUri")
                        } else {
                            android.util.Log.w("FileManager", "Failed to delete original document (deleteDocument returned false): $sourceFileUri")
                        }
                    } catch (e: SecurityException) {
                        android.util.Log.e("FileManager", "SecurityException when trying to delete original document: $sourceFileUri", e)
                        // This often means the app doesn't have permission to delete this URI.
                        // It might require persistent permissions to a document tree, or the URI itself isn't deletable by this app.
                    } catch (e: Exception) {
                        android.util.Log.e("FileManager", "Exception when trying to delete original document: $sourceFileUri", e)
                    }
                } else {
                     android.util.Log.w("FileManager", "Original file at $sourceFileUri was not a file:// URI or a deletable Document URI. Manual deletion might be required by user.")
                }
            }

            return destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            // If copy failed, delete the partially created file
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            return null
        }
    }
}
