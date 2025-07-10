package com.example.secure.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAllFiles: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToVideos: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onImportFile: () -> Unit,
    onOpenSettings: () -> Unit,
    isImporting: Boolean, // New state to control dialog visibility
    importProgress: Float // New state for progress (0.0 to 1.0)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("iSecure") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onImportFile) {
                Icon(Icons.Default.Add, contentDescription = "Import File")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CategoryCard(
                        icon = Icons.Default.Folder,
                        title = "All Files",
                        subtitle = "0 folders, 0 files", // Placeholder
                        onClick = onNavigateToAllFiles
                    )
                }
                item {
                    CategoryCard(
                        icon = Icons.Default.Image,
                        title = "Images",
                        subtitle = "0 files, 0 MB", // Placeholder
                        onClick = onNavigateToImages
                    )
                }
                item {
                    CategoryCard(
                        icon = Icons.Default.Videocam,
                        title = "Videos",
                        subtitle = "0 files, 0 MB", // Placeholder
                        onClick = onNavigateToVideos
                    )
                }
                item {
                    CategoryCard(
                        icon = Icons.Default.Article,
                        title = "Documents",
                        subtitle = "0 files, 0 MB", // Placeholder
                        onClick = onNavigateToDocuments
                    )
                }
            }

            if (isImporting) {
                FileImportDialog(progress = importProgress)
            }
        }
    }
}

@Composable
fun FileImportDialog(progress: Float) {
    AlertDialog(
        onDismissRequest = { /* Cannot be dismissed by user */ },
        title = { Text("Importing File...") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (progress < 0) { // Indeterminate progress
                    CircularProgressIndicator()
                } else { // Determinate progress
                    CircularProgressIndicator(progress = progress)
                }
                Text(if (progress < 0) "Processing..." else "${(progress * 100).toInt()}%")
            }
        },
        confirmButton = { /* No confirm button during progress */ },
        dismissButton = { /* No dismiss button during progress */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

// Removed corrupted duplicated code block that was here

@Composable
fun CategoryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Navigate")
        }
    }
}
