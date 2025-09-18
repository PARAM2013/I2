package com.example.secure.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.secure.ui.dashboard.ImportProgress

@Composable
fun SimpleImportDialog(
    importProgress: ImportProgress,
    onCancel: () -> Unit
) {
    android.util.Log.d("SimpleImportDialog", "Dialog called with isImporting=${importProgress.isImporting}")
    
    if (importProgress.isImporting) {
        android.util.Log.d("SimpleImportDialog", "Showing simple dialog")
        
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Importing Files...",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = { importProgress.overallProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "${importProgress.currentFileIndex} of ${importProgress.totalFiles} files"
                    )
                    
                    if (importProgress.currentFileName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Processing: ${importProgress.currentFileName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}