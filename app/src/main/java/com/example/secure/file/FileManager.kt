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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above, if not using MANAGE_EXTERNAL_STORAGE,
            // direct access to custom public directories like ".iSecureVault" is problematic.
            // This check might need to be adapted based on how Scoped Storage is handled.
            // For now, assume we primarily need READ/WRITE for legacy or if MANAGE_EXTERNAL_STORAGE is granted.
            // return Environment.isExternalStorageManager() // If targeting MANAGE_EXTERNAL_STORAGE
            // If not using MANAGE_EXTERNAL_STORAGE, then standard permissions are for media collections or SAF.
            // For simplicity in this step, we check traditional permissions, but this is a complex area for API 30+.
             return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED
        }
        return true // Permissions are granted by default on older versions
    }

    fun requestStoragePermissions(activity: Activity) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // WRITE_EXTERNAL_STORAGE is only needed up to API 28 (Android 9).
        // From API 29 (Android 10), it's effectively granted if READ_EXTERNAL_STORAGE is, for app's own files.
        // For modifying other apps' files or broad access, Scoped Storage rules apply.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Regarding MANAGE_EXTERNAL_STORAGE for Android 11+
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
        //     // Intent to request MANAGE_EXTERNAL_STORAGE - this is a special permission
        //     // val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        //     // intent.data = Uri.parse("package:${activity.packageName}")
        //     // activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE_PERMISSION_CODE)
        //     // For now, we are not explicitly requesting MANAGE_EXTERNAL_STORAGE here.
        //     // We rely on legacy storage for existing files or hope direct creation works.
        // }


        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), REQUEST_STORAGE_PERMISSION_CODE)
        }
    }

    // TODO: Add methods for file operations like:
    // fun importFile(sourceFile: File, context: Context): File? (moves file to vault)
    // fun unhideFile(vaultFile: File, context: Context): File? (moves file from vault to unhide dir)
    // fun deleteFileFromVault(fileInVault: File): Boolean
    // fun listFilesInVault(subDirectory: String? = null): List<File>
    // fun createSubFolderInVault(folderName: String): File?
}
