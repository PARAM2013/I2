package com.example.secure.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.secure.ui.dashboard.UnhideProgress

@Composable
fun UnhideProgressDialog(
    unhideProgress: UnhideProgress,
    onCancel: () -> Unit
) {
    // Debug logging
    android.util.Log.d("UnhideProgressDialog", "Dialog called with isUnhiding=${unhideProgress.isUnhiding}, totalFiles=${unhideProgress.totalFiles}")
    
    if (unhideProgress.isUnhiding) {
        android.util.Log.d("UnhideProgressDialog", "Showing unhide progress dialog")
        Dialog(
            onDismissRequest = { /* Prevent dismissal by clicking outside */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with cancel button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Unhiding Files",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (unhideProgress.canCancel) {
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Unhide",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = { unhideProgress.overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Progress text
                    Text(
                        text = "${unhideProgress.currentFileIndex} of ${unhideProgress.totalFiles} items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Current file name
                    if (unhideProgress.currentFileName.isNotEmpty()) {
                        Text(
                            text = "Processing: ${unhideProgress.currentFileName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Success/Failed counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${unhideProgress.successfulUnhides}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Unhidden",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        if (unhideProgress.failedUnhides > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${unhideProgress.failedUnhides}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Failed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (unhideProgress.canCancel) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Unhide")
                        }
                    }
                }
            }
        }
    }
}