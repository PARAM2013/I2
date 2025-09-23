package com.example.secure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.secure.ui.composables.ImportProgressDialog
import com.example.secure.ui.composables.ImportSuccessDialog
import com.example.secure.ui.dashboard.ImportProgress
import com.example.secure.ui.theme.ISecureTheme
import java.util.ArrayList

class FileReceiverActivity : ComponentActivity() {

    private val importProgressState = mutableStateOf(ImportProgress())
    private val showSuccessDialog = mutableStateOf(false)
    private val successCount = mutableStateOf(0)
    private val failedCount = mutableStateOf(0)

    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == FileImportService.BROADCAST_ACTION_PROGRESS) {
                val progress = intent.getIntExtra(FileImportService.EXTRA_PROGRESS, 0)
                val total = intent.getIntExtra(FileImportService.EXTRA_TOTAL, 0)
                val fileName = intent.getStringExtra(FileImportService.EXTRA_FILE_NAME) ?: ""
                val finished = intent.getBooleanExtra(FileImportService.EXTRA_FINISHED, false)

                if (finished) {
                    successCount.value = intent.getIntExtra(FileImportService.EXTRA_SUCCESS_COUNT, 0)
                    failedCount.value = intent.getIntExtra(FileImportService.EXTRA_FAILED_COUNT, 0)
                    importProgressState.value = ImportProgress(isImporting = false)
                    showSuccessDialog.value = true
                } else {
                    importProgressState.value = ImportProgress(
                        isImporting = true,
                        currentFileIndex = progress + 1,
                        totalFiles = total,
                        currentFileName = fileName,
                        overallProgress = (progress.toFloat() / total)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uris = intent.getUris()
        if (uris.isEmpty()) {
            Toast.makeText(this, "No files to import.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            progressReceiver,
            IntentFilter(FileImportService.BROADCAST_ACTION_PROGRESS)
        )

        val serviceIntent = Intent(this, FileImportService::class.java).apply {
            action = FileImportService.ACTION_START_IMPORT
            putParcelableArrayListExtra(FileImportService.EXTRA_FILE_URIS, ArrayList(uris))
            putExtra(FileImportService.EXTRA_SHOW_NOTIFICATION, true)
        }
        startService(serviceIntent)

        setContent {
            ISecureTheme {
                if (importProgressState.value.isImporting) {
                    ImportProgressDialog(
                        importProgress = importProgressState.value,
                        onCancel = {
                            // Cancellation from share UI is not supported in this simplified version
                        }
                    )
                }

                if (showSuccessDialog.value) {
                    ImportSuccessDialog(
                        successCount = successCount.value,
                        failedCount = failedCount.value,
                        onDismiss = {
                            finish()
                        },
                        onViewFiles = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver)
    }

    private fun Intent.getUris(): List<Uri> {
        return when (action) {
            Intent.ACTION_SEND -> {
                val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    getParcelableExtra(Intent.EXTRA_STREAM)
                }
                if (uri != null) listOf(uri) else emptyList()
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                }
                uris?.toList() ?: emptyList()
            }
            else -> emptyList()
        }
    }
}
