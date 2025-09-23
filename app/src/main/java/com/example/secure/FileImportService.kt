package com.example.secure

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.secure.file.FileManager
import kotlinx.coroutines.*
import java.util.ArrayList

class FileImportService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var notificationManager: NotificationManager

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "FileImportChannel"
        const val NOTIFICATION_ID = 1

        const val ACTION_START_IMPORT = "com.example.secure.action.START_IMPORT"
        const val EXTRA_FILE_URIS = "com.example.secure.extra.FILE_URIS"
        const val EXTRA_SHOW_NOTIFICATION = "com.example.secure.extra.SHOW_NOTIFICATION"

        const val BROADCAST_ACTION_PROGRESS = "com.example.secure.broadcast.IMPORT_PROGRESS"
        const val EXTRA_PROGRESS = "com.example.secure.extra.PROGRESS"
        const val EXTRA_TOTAL = "com.example.secure.extra.TOTAL"
        const val EXTRA_FILE_NAME = "com.example.secure.extra.FILE_NAME"
        const val EXTRA_FINISHED = "com.example.secure.extra.FINISHED"
        const val EXTRA_SUCCESS_COUNT = "com.example.secure.extra.SUCCESS_COUNT"
        const val EXTRA_FAILED_COUNT = "com.example.secure.extra.FAILED_COUNT"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_IMPORT) {
            val fileUris = intent.getParcelableArrayListExtra<Uri>(EXTRA_FILE_URIS)
            val showNotification = intent.getBooleanExtra(EXTRA_SHOW_NOTIFICATION, false)

            if (fileUris != null && fileUris.isNotEmpty()) {
                if (showNotification) {
                    startForeground(NOTIFICATION_ID, createNotificationBuilder(0, fileUris.size, "Starting import...").build())
                }
                scope.launch {
                    importFiles(fileUris, showNotification)
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun importFiles(uris: List<Uri>, showNotification: Boolean) {
        var successCount = 0
        var failedCount = 0

        uris.forEachIndexed { index, uri ->
            val fileName = getFileNameFromUri(uri) ?: "Unknown file"

            sendProgressBroadcast(index, uris.size, fileName, false, 0, 0)
            if (showNotification) {
                val notification = createNotificationBuilder(index, uris.size, "Importing: $fileName").build()
                notificationManager.notify(NOTIFICATION_ID, notification)
            }

            try {
                val result = FileManager.importFile(uri, applicationContext)
                if (result != null) {
                    successCount++
                } else {
                    failedCount++
                }
            } catch (e: Exception) {
                Log.e("FileImportService", "Error importing file", e)
                failedCount++
            }
        }

        sendProgressBroadcast(uris.size, uris.size, "Finished", true, successCount, failedCount)

        if (showNotification) {
            val finalNotification = createFinalNotification(successCount, failedCount)
            notificationManager.notify(NOTIFICATION_ID + 1, finalNotification.build())
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        stopSelf()
    }

    private fun sendProgressBroadcast(progress: Int, total: Int, fileName: String, finished: Boolean, successCount: Int, failedCount: Int) {
        val intent = Intent(BROADCAST_ACTION_PROGRESS).apply {
            putExtra(EXTRA_PROGRESS, progress)
            putExtra(EXTRA_TOTAL, total)
            putExtra(EXTRA_FILE_NAME, fileName)
            putExtra(EXTRA_FINISHED, finished)
            putExtra(EXTRA_SUCCESS_COUNT, successCount)
            putExtra(EXTRA_FAILED_COUNT, failedCount)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "File Import",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationBuilder(progress: Int, total: Int, contentText: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Importing files")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_add_file)
            .setProgress(total, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    private fun createFinalNotification(successCount: Int, failedCount: Int): NotificationCompat.Builder {
        val contentText = if (failedCount > 0) {
            "Completed. $successCount succeeded, $failedCount failed."
        } else {
            "Import complete. $successCount files imported."
        }
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Import finished")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_add_file)
            .setProgress(0, 0, false)
            .setOngoing(false)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
