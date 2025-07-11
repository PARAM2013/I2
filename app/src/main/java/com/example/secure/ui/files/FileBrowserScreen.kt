package com.example.secure.ui.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp // Added import for dp
import com.example.secure.ui.theme.ISecureTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    onNavigateUp: () -> Unit
) {
    ISecureTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("All Files") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back" // TODO: Use string resource
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp), // Used dp here
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "File Browser Screen - Content TBD",
                    style = MaterialTheme.typography.headlineMedium
                )
                // TODO: Implement file and folder listing logic here
                // This will be similar to the old MainDashboardScreen's LazyColumn/LazyVerticalGrid
                // and will need its own ViewModel (e.g., FileBrowserViewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileBrowserScreenPreview() {
    FileBrowserScreen(onNavigateUp = {})
}
