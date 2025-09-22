package com.example.secure.util

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object FileOperations {
    
    private const val TAG = "FileOperations"
    
    /**
     * Opens a file using the appropriate system app
     */
    fun openFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val mimeType = getMimeType(file)
            Log.d(TAG, "Determined MIME type for ${file.name}: $mimeType")

            val specificIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Always create a chooser for the specific intent first.
            // This ensures that even if resolveActivity returns null (no default app set),
            // the system still tries to present relevant options.
            val chooserIntent = Intent.createChooser(specificIntent, "Open ${file.name}")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Check if there are any activities that can handle the chooser intent itself
            val resolvedChooserActivities: List<ResolveInfo> = context.packageManager.queryIntentActivities(chooserIntent, 0)
            
            if (resolvedChooserActivities.isNotEmpty()) {
                context.startActivity(chooserIntent)
                Log.d(TAG, "Opening file: ${file.name} with specific mime type: $mimeType using chooser. Resolved activities: ${resolvedChooserActivities.size}")
            } else {
                // Fallback to generic intent if even the specific chooser yields no options
                Log.d(TAG, "No activities resolved for specific intent chooser. Falling back to generic.")
                val genericIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                val genericChooserIntent = Intent.createChooser(genericIntent, "Open ${file.name}")
                genericChooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                val resolvedGenericChooserActivities: List<ResolveInfo> = context.packageManager.queryIntentActivities(genericChooserIntent, 0)

                if (resolvedGenericChooserActivities.isNotEmpty()) {
                    context.startActivity(genericChooserIntent)
                    Log.d(TAG, "Opening file with generic intent: ${file.name} using chooser. Resolved activities: ${resolvedGenericChooserActivities.size}")
                } else {
                    Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "No app found to open file: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file: ${file.name}", e)
            Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Shares a file using the system share dialog
     */
    fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val mimeType = getMimeType(file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Sharing ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "Shared from iSecure Vault")
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share ${file.name}")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            Log.d(TAG, "Sharing file: ${file.name} with mime type: $mimeType")
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file: ${file.name}", e)
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Shares multiple files using the system share dialog
     */
    fun shareMultipleFiles(context: Context, files: List<File>) {
        try {
            if (files.isEmpty()) {
                Toast.makeText(context, "No files to share", Toast.LENGTH_SHORT).show()
                return
            }
            
            val uris = files.map { file ->
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }
            
            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*" // Mixed file types
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Sharing ${files.size} files")
                putExtra(Intent.EXTRA_TEXT, "Shared from iSecure Vault")
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share ${files.size} files")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            Log.d(TAG, "Sharing ${files.size} files")
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing multiple files", e)
            Toast.makeText(context, "Error sharing files: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Gets the MIME type for a file based on its extension
     */
    private fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            
            // Videos
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "webm" -> "video/webm"
            "mkv" -> "video/x-matroska"
            "3gp" -> "video/3gpp"
            
            // Documents
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "rtf" -> "application/rtf"
            "odt" -> "application/vnd.oasis.opendocument.text"
            "ods" -> "application/vnd.oasis.opendocument.spreadsheet"
            "odp" -> "application/vnd.oasis.opendocument.presentation"
            
            // Archives
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            
            // Audio
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            
            // Other
            "json" -> "application/json"
            "xml" -> "application/xml"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            
            else -> "*/*"
        }
    }
    
    /**
     * Checks if a file type can be opened by the system
     */
    fun canOpenFile(context: Context, file: File): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val mimeType = getMimeType(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if file can be opened: ${file.name}", e)
            false
        }
    }
}