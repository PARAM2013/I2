package com.example.secure.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object ApkBackupUtil {
    
    private const val APK_BACKUP_PREF = "apk_backup_done"
    
    /**
     * Backs up the current APK to Downloads folder on first launch
     */
    fun backupApkToDownloads(context: Context): Boolean {
        // Check if backup already done
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean(APK_BACKUP_PREF, false)) {
            Log.d("ApkBackup", "APK backup already exists, skipping")
            return true
        }
        
        return try {
            val applicationInfo = context.applicationInfo
            val sourceApk = File(applicationInfo.sourceDir)
            
            // Get Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            // Create backup filename with version
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val backupApkName = "iSecureVault_v${versionName}_backup.apk"
            val backupApk = File(downloadsDir, backupApkName)
            
            // Skip if backup already exists
            if (backupApk.exists()) {
                Log.d("ApkBackup", "Backup APK already exists: ${backupApk.absolutePath}")
                prefs.edit().putBoolean(APK_BACKUP_PREF, true).apply()
                return true
            }
            
            // Copy APK to Downloads
            FileInputStream(sourceApk).use { inputStream ->
                FileOutputStream(backupApk).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // Set readable permissions
            backupApk.setReadable(true, false)
            
            // Mark backup as done
            prefs.edit().putBoolean(APK_BACKUP_PREF, true).apply()
            
            Log.d("ApkBackup", "APK backed up successfully to: ${backupApk.absolutePath}")
            true
            
        } catch (e: IOException) {
            Log.e("ApkBackup", "Failed to backup APK: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e("ApkBackup", "Unexpected error during APK backup: ${e.message}", e)
            false
        }
    }
    
    /**
     * Gets the backup APK file if it exists
     */
    fun getBackupApkFile(context: Context): File? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val backupApkName = "iSecureVault_v${versionName}_backup.apk"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupApk = File(downloadsDir, backupApkName)
            
            if (backupApk.exists()) backupApk else null
        } catch (e: Exception) {
            Log.e("ApkBackup", "Error getting backup APK file: ${e.message}", e)
            null
        }
    }
    
    /**
     * Checks if APK backup exists in Downloads
     */
    fun hasBackupApk(context: Context): Boolean {
        return getBackupApkFile(context) != null
    }
}