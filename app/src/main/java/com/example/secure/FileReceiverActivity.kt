package com.example.secure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.secure.ui.composables.ImportProgressDialog
import com.example.secure.ui.composables.ImportSuccessDialog
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme

class FileReceiverActivity : ComponentActivity() {

    private val viewModel: MainDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uris = intent.getUris()
        if (uris.isEmpty()) {
            Toast.makeText(this, "No files to import.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.importFiles(uris)

        setContent {
            ISecureTheme {
                val uiState by viewModel.uiState.collectAsState()

                if (uiState.importProgress.isImporting) {
                    ImportProgressDialog(
                        importProgress = uiState.importProgress,
                        onCancel = {
                            viewModel.cancelImport()
                            finish()
                        }
                    )
                }

                if (uiState.showImportSuccessDialog) {
                    ImportSuccessDialog(
                        successCount = uiState.lastImportSuccessCount,
                        failedCount = uiState.lastImportFailedCount,
                        onDismiss = {
                            viewModel.dismissImportSuccessDialog()
                            finish()
                        },
                        onViewFiles = {
                            // In this context, just close the dialog and finish.
                            viewModel.dismissImportSuccessDialog()
                            finish()
                        }
                    )
                }

                // This effect will finish the activity once the import process is truly over
                // (i.e., no progress dialog and no success dialog are showing).
                LaunchedEffect(uiState.importProgress.isImporting, uiState.showImportSuccessDialog) {
                    if (!uiState.importProgress.isImporting && !uiState.showImportSuccessDialog) {
                        // A small delay to ensure any final toasts from the ViewModel are shown
                        kotlinx.coroutines.delay(500)
                        finish()
                    }
                }
            }
        }
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
