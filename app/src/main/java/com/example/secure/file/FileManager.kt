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
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.exifinterface.media.ExifInterface
import com.example.secure.auth.PinManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import com.example.secure.R

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
object FileManager {

    const val VAULT_FOLDER_NAME = ".iSecureVault" // Hidden folder
    const val UNHIDE_FOLDER_NAME = "SecureUnhide" // In Downloads for unhidden files

    // Request code for storage permissions
    const val REQUEST_STORAGE_PERMISSION_CODE = 101
    const val REQUEST_MANAGE_STORAGE_PERMISSION_CODE = 102 // For Android 11+

    private var permissionsCached = false
    private var hasPermissions = false

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
        if (permissionsCached) {
            return hasPermissions
        }

        hasPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED
        } else {
            true // Below Android M, permissions are granted at install time
        }

        permissionsCached = true
        return hasPermissions
    }

    fun clearPermissionCache() {
        permissionsCached = false
        hasPermissions = false
    }

    fun requestStoragePermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above - Request MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = "package:${activity.packageName}".toUri()
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

    fun unhideFile(vaultFile: File, context: Context, destinationDir: File): File? {
        if (!vaultFile.exists() || !vaultFile.isFile) {
            Log.w("FileManager", "Unhide failed: source file does not exist or is not a file: ${vaultFile.path}")
            return null
        }
        if (!vaultFile.path.startsWith(getVaultDirectory().path)) {
            Log.w("FileManager", "Attempt to unhide file outside vault: ${vaultFile.path}")
            return null
        }

        // Ensure the specific destination directory exists
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            Log.e("FileManager", "Failed to create specific destination directory for unhide: ${destinationDir.path}")
            return null
        }

        var destinationFile = File(destinationDir, vaultFile.name)
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
            destinationFile = File(destinationDir, newName) // Corrected to use destinationDir
        }

        try {
            Log.d("FileManager", "Attempting to unhide ${vaultFile.name} to ${destinationFile.absolutePath}")
            if (vaultFile.renameTo(destinationFile)) {
                Log.d("FileManager", "Successfully unhidden ${vaultFile.name} via rename.")
                // Optionally, trigger a media scan for the unhidden file so it appears in galleries etc.
                android.media.MediaScannerConnection.scanFile(context, arrayOf(destinationFile.absolutePath), null, null)
                return destinationFile
            } else {
                Log.w("FileManager", "Rename failed for ${vaultFile.name}, attempting copy and delete.")
                // Fallback: try copy and delete if rename fails
                vaultFile.copyTo(destinationFile, overwrite = true) // overwrite should be safe due to conflict handling
                if (destinationFile.exists() && destinationFile.length() == vaultFile.length()) {
                    vaultFile.delete()
                    Log.d("FileManager", "Successfully unhidden ${vaultFile.name} via copy/delete.")
                    android.media.MediaScannerConnection.scanFile(context, arrayOf(destinationFile.absolutePath), null, null)
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
        val size: Long,
        val thumbnail: Bitmap? = null, // Add this line
        val duration: String? = null
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
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "zip", "rar" -> FileCategory.DOCUMENT // Add zip and rar
            else -> FileCategory.OTHER
        }
    }

    fun getIconForFile(fileName: String): Int {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> R.drawable.ic_pdf
            "doc", "docx" -> R.drawable.ic_doc
            "xls", "xlsx" -> R.drawable.ic_xls
            "ppt", "pptx" -> R.drawable.ic_ppt
            "zip", "rar" -> R.drawable.ic_zip
            "txt" -> R.drawable.ic_txt
            else -> R.drawable.ic_file
        }
    }

    fun getVideoDuration(file: File): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            val timeInMillis = time?.toLongOrNull()
            if (timeInMillis != null) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) -
                        TimeUnit.MINUTES.toSeconds(minutes)
                String.format("%d:%02d", minutes, seconds)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FileManager", "Failed to get duration for ${file.name}", e)
            null
        }
    }

    fun listFilesInVault(directory: File = getVaultDirectory(), context: Context? = null): VaultStats {
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
                var thumbnail: Bitmap? = null
                var duration: String? = null
                
                when (category) {
                    FileCategory.PHOTO -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                thumbnail = ThumbnailUtils.createImageThumbnail(item, android.util.Size(256, 256), null)
                            } catch (e: Exception) {
                                Log.e("FileManager", "Failed to create image thumbnail for ${item.name}", e)
                            }
                        } else {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(item.path, MediaStore.Images.Thumbnails.MINI_KIND)
                        }
                    }
                    FileCategory.VIDEO -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(item, android.util.Size(256, 256), null)
                        } else {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(item.path, MediaStore.Video.Thumbnails.MINI_KIND)
                        }
                        duration = getVideoDuration(item)
                    }
                    FileCategory.DOCUMENT -> {
                        // Generate PDF thumbnail if it's a PDF file and context is available
                        if (item.extension.lowercase() == "pdf" && context != null) {
                            try {
                                thumbnail = com.example.secure.util.PdfThumbnailGenerator.generateThumbnail(
                                    context = context,
                                    pdfFile = item,
                                    width = 200,
                                    height = 300
                                )
                            } catch (e: Exception) {
                                Log.w("FileManager", "Failed to generate PDF thumbnail for ${item.name}", e)
                            }
                        }
                    }
                    else -> {
                        // No thumbnail generation for other categories
                    }
                }
                
                val vaultFile = VaultFile(item, category, size, thumbnail, duration)

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

    fun listAllFilesRecursively(directory: File, context: Context? = null): List<VaultFile> {
        val allFiles = mutableListOf<VaultFile>()
        val filesAndFolders = directory.listFiles() ?: return allFiles
        for (file in filesAndFolders) {
            if (file.isDirectory) {
                allFiles.addAll(listAllFilesRecursively(file, context))
            } else {
                val category = getFileCategory(file.name)
                val size = file.length()
                var thumbnail: Bitmap? = null
                var duration: String? = null

                when (category) {
                    FileCategory.PHOTO -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                thumbnail = ThumbnailUtils.createImageThumbnail(file, android.util.Size(256, 256), null)
                            } catch (e: Exception) {
                                Log.e("FileManager", "Failed to create image thumbnail for ${file.name}", e)
                            }
                        } else {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(file.path, MediaStore.Images.Thumbnails.MINI_KIND)
                        }
                    }
                    FileCategory.VIDEO -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(file, android.util.Size(256, 256), null)
                        } else {
                            thumbnail = ThumbnailUtils.createVideoThumbnail(file.path, MediaStore.Video.Thumbnails.MINI_KIND)
                        }
                        duration = getVideoDuration(file)
                    }
                    FileCategory.DOCUMENT -> {
                        if (file.extension.lowercase() == "pdf" && context != null) {
                            try {
                                thumbnail = com.example.secure.util.PdfThumbnailGenerator.generateThumbnail(
                                    context = context,
                                    pdfFile = file,
                                    width = 200,
                                    height = 300
                                )
                            } catch (e: Exception) {
                                Log.w("FileManager", "Failed to generate PDF thumbnail for ${file.name}", e)
                            }
                        }
                    }
                    else -> {
                        // No thumbnail generation for other categories
                    }
                }
                allFiles.add(VaultFile(file, category, size, thumbnail, duration))
            }
        }
        return allFiles
    }

    fun listAllFoldersRecursively(directory: File): List<File> {
        val allFolders = mutableListOf<File>()
        val filesAndFolders = directory.listFiles() ?: return allFolders
        for (file in filesAndFolders) {
            if (file.isDirectory) {
                allFolders.add(file)
                allFolders.addAll(listAllFoldersRecursively(file))
            }
        }
        return allFolders
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

    fun importFile(sourceFileUri: android.net.Uri, context: Context, targetRelativePath: String? = null): Pair<File, android.net.Uri>? {
        val contentResolver = context.contentResolver
        var fileName: String? = null

        // Get file name and size from URI
        contentResolver.query(sourceFileUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
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

            var tempFileToProcess: File? = null
            var streamToDelete: InputStream?

            if (PinManager.isMetadataRemovalEnabled(context) && (extension.equals("jpg", true) || extension.equals("jpeg", true) || extension.equals("png", true))) {
                Log.d("FileManager", "Metadata removal enabled for $fileName")
                try {
                    // Copy to a temporary file to use with ExifInterface
                    tempFileToProcess = File.createTempFile("metadata_strip_", ".${extension}", context.cacheDir)
                    contentResolver.openInputStream(sourceFileUri)?.use { input ->
                        FileOutputStream(tempFileToProcess).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (tempFileToProcess.exists() && tempFileToProcess.length() > 0) {
                        Log.d("FileManager", "Copied to temp file for metadata stripping: ${tempFileToProcess.absolutePath}")
                        val exifInterface = ExifInterface(tempFileToProcess.absolutePath)
                        // Clear common tags. Add more as needed.
                        exifInterface.setAttribute(ExifInterface.TAG_DATETIME, null)
                        exifInterface.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, null)
                        exifInterface.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, null)
                        exifInterface.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, null)
                        exifInterface.setAttribute(ExifInterface.TAG_F_NUMBER, null)
                        exifInterface.setAttribute(ExifInterface.TAG_FLASH, null)
                        exifInterface.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null)
                        exifInterface.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, null)
                        exifInterface.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, null)
                        exifInterface.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, null)
                        exifInterface.setAttribute(ExifInterface.TAG_ISO_SPEED, null)
                        exifInterface.setAttribute(ExifInterface.TAG_MAKE, null)
                        exifInterface.setAttribute(ExifInterface.TAG_MODEL, null)
                        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, null)
                        exifInterface.setAttribute(ExifInterface.TAG_SOFTWARE, null)
                        // For PNG, some specific chunks might need different handling,
                        // but ExifInterface has some support.
                        // Add any other specific tags you want to remove.

                        exifInterface.saveAttributes() // This saves changes to the tempFileToProcess
                        Log.d("FileManager", "Metadata stripped from temp file.")
                        streamToDelete = tempFileToProcess.inputStream()
                    } else {
                        Log.w("FileManager", "Temp file for metadata stripping is empty or does not exist.")
                        // Fallback to original stream if temp file creation failed
                        streamToDelete = contentResolver.openInputStream(sourceFileUri)
                    }
                } catch (e: Exception) {
                    Log.e("FileManager", "Error during metadata stripping for $fileName, falling back to original.", e)
                    // Fallback to original stream on error
                    streamToDelete = contentResolver.openInputStream(sourceFileUri)
                    tempFileToProcess?.delete() // Clean up if partially created
                    tempFileToProcess = null
                }
            } else {
                // No metadata stripping, use original stream
                streamToDelete = contentResolver.openInputStream(sourceFileUri)
            }

            streamToDelete?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run { Log.e("FileManager", "importFile: Failed to open input stream for URI: $sourceFileUri"); tempFileToProcess?.delete(); return null }

            tempFileToProcess?.delete() // Clean up the temp file if it was used

            Log.d("FileManager", "importFile: File copied successfully to ${destinationFile.absolutePath}")

            // Note: Original files are not deleted to avoid permission issues and provide better user experience
            // Users can manually delete original files if desired
            return Pair(destinationFile, sourceFileUri)
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

    /**
     * Moves a file from one location to another within the vault.
     * Handles renaming and ensures the destination directory exists.
     *
     * @param sourceFile The file to be moved. Must exist and be within the vault.
     * @param destinationDirectory The directory where the file should be moved to. Must exist and be within the vault.
     * @return The new File object if successful, null otherwise.
     */
    fun moveFileInVault(sourceFile: File, destinationDirectory: File): File? {
        if (!sourceFile.exists() || !sourceFile.isFile) {
            Log.w("FileManager", "moveFileInVault: Source file does not exist or is not a file: ${sourceFile.absolutePath}")
            return null
        }
        if (!sourceFile.path.startsWith(getVaultDirectory().path)) {
            Log.w("FileManager", "moveFileInVault: Source file is not within the vault: ${sourceFile.absolutePath}")
            return null
        }
        if (!destinationDirectory.exists() || !destinationDirectory.isDirectory) {
            Log.w("FileManager", "moveFileInVault: Destination directory does not exist or is not a directory: ${destinationDirectory.absolutePath}")
            return null
        }
        if (!destinationDirectory.path.startsWith(getVaultDirectory().path)) {
            Log.w("FileManager", "moveFileInVault: Destination directory is not within the vault: ${destinationDirectory.absolutePath}")
            return null
        }

        // Check if source and destination are the same
        if (sourceFile.parentFile?.absolutePath == destinationDirectory.absolutePath) {
            Log.i("FileManager", "moveFileInVault: Source and destination directories are the same. No move needed for ${sourceFile.name}.")
            return sourceFile
        }

        var newFile = File(destinationDirectory, sourceFile.name)
        var counter = 1
        val nameWithoutExt = sourceFile.nameWithoutExtension
        val extension = sourceFile.extension

        // Handle file name conflicts at the destination
        while (newFile.exists()) {
            val uniqueName = if (extension.isEmpty()) {
                "${nameWithoutExt} (${counter++})"
            } else {
                "${nameWithoutExt} (${counter++}).$extension"
            }
            newFile = File(destinationDirectory, uniqueName)
        }

        return try {
            if (sourceFile.renameTo(newFile)) {
                Log.d("FileManager", "moveFileInVault: Successfully moved '${sourceFile.name}' to '${newFile.absolutePath}'")
                newFile
            } else {
                Log.e("FileManager", "moveFileInVault: Failed to move '${sourceFile.name}' to '${newFile.absolutePath}' using renameTo().")
                null
            }
        } catch (e: SecurityException) {
            Log.e("FileManager", "moveFileInVault: SecurityException while moving '${sourceFile.name}' to '${newFile.absolutePath}': ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("FileManager", "moveFileInVault: Unexpected error while moving '${sourceFile.name}' to '${newFile.absolutePath}': ${e.message}")
            null
        }
    }

    fun unhideFolderRecursive(folderToUnhide: File, context: Context, baseUnhideDir: File): Boolean {
        if (!folderToUnhide.exists() || !folderToUnhide.isDirectory) {
            Log.w("FileManager", "unhideFolderRecursive: source folder does not exist or is not a directory: ${folderToUnhide.path}")
            return false
        }
        if (!folderToUnhide.path.startsWith(getVaultDirectory().path)) {
            Log.w("FileManager", "unhideFolderRecursive: Attempt to unhide folder outside vault: ${folderToUnhide.path}")
            return false
        }

        // Determine target folder name and handle conflicts in the baseUnhideDir
        var targetUnhiddenFolderPath = File(baseUnhideDir, folderToUnhide.name)
        var counter = 1
        val originalName = folderToUnhide.name
        while (targetUnhiddenFolderPath.exists()) {
            targetUnhiddenFolderPath = File(baseUnhideDir, "$originalName (${counter++})")
        }

        if (!targetUnhiddenFolderPath.mkdirs()) {
            Log.e("FileManager", "unhideFolderRecursive: Failed to create target directory in unhide location: ${targetUnhiddenFolderPath.path}")
            return false
        }
        Log.d("FileManager", "unhideFolderRecursive: Created target folder ${targetUnhiddenFolderPath.path}")

        var allSuccessful = true
        folderToUnhide.listFiles()?.forEach { item ->
            if (item.isDirectory) {
                // Recursively unhide subfolder into the newly created targetUnhiddenFolderPath
                if (!unhideFolderRecursive(item, context, targetUnhiddenFolderPath)) {
                    allSuccessful = false
                    Log.e("FileManager", "unhideFolderRecursive: Failed to unhide subfolder: ${item.name}")
                    // Decide on error handling: continue or stop? For now, continue and report overall status.
                }
            } else { // It's a file
                // Unhide file into the newly created targetUnhiddenFolderPath
                val unhiddenFile = unhideFile(item, context, targetUnhiddenFolderPath)
                if (unhiddenFile == null) {
                    allSuccessful = false
                    Log.e("FileManager", "unhideFolderRecursive: Failed to unhide file: ${item.name}")
                }
            }
        }

        if (allSuccessful) {
            // If all contents were successfully moved, delete the original folder from the vault
            if (folderToUnhide.deleteRecursively()) {
                Log.d("FileManager", "unhideFolderRecursive: Successfully unhidden and deleted original folder: ${folderToUnhide.name}")
                return true
            } else {
                Log.e("FileManager", "unhideFolderRecursive: Successfully unhidden contents, but failed to delete original folder: ${folderToUnhide.name}")
                // This is a partial success state. Contents are unhidden, but original is not cleaned up.
                return false // Or true, depending on desired strictness. Let's say false if cleanup fails.
            }
        } else {
            Log.e("FileManager", "unhideFolderRecursive: Failed to unhide some contents of folder: ${folderToUnhide.name}. Original folder not deleted.")
            // Potentially attempt to revert/delete the partially created targetUnhiddenFolderPath?
            // For now, leave as is to allow user to see what was unhidden.
            return false
        }
    }

    fun renameItemInVault(itemToRename: File, newName: String, context: Context): File? {
        if (newName.isBlank() || newName.contains(File.separatorChar) || newName == "." || newName == "..") {
            Log.w("FileManager", "renameItemInVault: Invalid new name provided: '$newName'")
            // Optionally, provide a more specific error message to the user via ViewModel
            // For now, just logging and returning null.
            return null
        }

        if (!itemToRename.exists()) {
            Log.w("FileManager", "renameItemInVault: Item to rename does not exist: ${itemToRename.absolutePath}")
            return null
        }

        if (!itemToRename.path.startsWith(getVaultDirectory().path)) {
            Log.w("FileManager", "renameItemInVault: Attempt to rename item outside vault: ${itemToRename.path}")
            return null
        }

        val parentDir = itemToRename.parentFile
        if (parentDir == null) {
            Log.e("FileManager", "renameItemInVault: Cannot get parent directory for ${itemToRename.absolutePath}")
            return null
        }

        val newFile = File(parentDir, newName)

        if (newFile.absolutePath == itemToRename.absolutePath) {
            Log.i("FileManager", "renameItemInVault: New name is the same as the old name. No action taken.")
            return itemToRename // Or null if no change should also be an error/no-op from caller's perspective
        }

        if (newFile.exists()) {
            Log.w("FileManager", "renameItemInVault: An item with the new name '$newName' already exists in ${parentDir.absolutePath}")
            // TODO: Propagate this specific error to ViewModel/UI e.g. using R.string.error_folder_exists or similar
            return null
        }

        return try {
            if (itemToRename.renameTo(newFile)) {
                Log.d("FileManager", "renameItemInVault: Successfully renamed '${itemToRename.name}' to '${newFile.name}'")
                // If it's a file, update media scanner
                if (newFile.isFile) {
                    android.media.MediaScannerConnection.scanFile(
                        context,
                        arrayOf(itemToRename.absolutePath, newFile.absolutePath), // Scan old and new paths
                        null,
                        null
                    )
                }
                newFile
            } else {
                Log.e("FileManager", "renameItemInVault: Failed to rename '${itemToRename.name}' to '${newName}' using renameTo()")
                null
            }
        } catch (e: SecurityException) {
            Log.e("FileManager", "renameItemInVault: SecurityException while renaming '${itemToRename.name}' to '$newName'", e)
            null
        } catch (e: Exception) {
            Log.e("FileManager", "renameItemInVault: Unexpected error while renaming '${itemToRename.name}' to '$newName'", e)
            null
        }
    }
}