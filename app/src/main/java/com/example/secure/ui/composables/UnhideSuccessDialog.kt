package com.example.secure.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun UnhideSuccessDialog(
    successCount: Int,
    failedCount: Int,
    onDismiss: () -> Unit,
    onViewFiles: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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
                // Success icon
                Icon(
                    imageVector = if (failedCount == 0) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (failedCount == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = if (failedCount == 0) "Unhide Complete!" else "Unhide Finished",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Results summary
                if (successCount > 0) {
                    Text(
                        text = "$successCount ${if (successCount == 1) "item" else "items"} successfully unhidden",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
                
                if (failedCount > 0) {
                    if (successCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = "$failedCount ${if (failedCount == 1) "item" else "items"} failed to unhide",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (onViewFiles != null) Arrangement.SpaceEvenly else Arrangement.Center
                ) {
                    if (onViewFiles != null && successCount > 0) {
                        TextButton(
                            onClick = {
                                onViewFiles()
                                onDismiss()
                            }
                        ) {
                            Text("View Files")
                        }
                    }
                    
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}