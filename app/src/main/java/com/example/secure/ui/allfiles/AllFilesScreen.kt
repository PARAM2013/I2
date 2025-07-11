package com.example.secure.ui.allfiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.secure.R
import com.example.secure.ui.theme.ISecureTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilesScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_all_files)) }, // Assume R.string.title_all_files exists
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back) // Assume R.string.action_back exists
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(all = 16.dp), // Replaced placeholder with direct value
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "This is the All Files Screen.",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "File and folder list will be displayed here.",
                style = MaterialTheme.typography.bodyLarge
            )
            // TODO: Implement actual file and folder listing using MainDashboardViewModel or similar
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllFilesScreenPreview() {
    ISecureTheme {
        AllFilesScreen(onNavigateBack = {})
    }
}

// Placeholder for common.ui.theme.Padding - replace with actual import or value
object common {
    object ui {
        object theme {
            object Padding {
                val Sixteen = 16 // Representing 16.dp, actual usage would be 16.dp
            }
        }
    }
}
// Placeholder strings - ensure these are in your strings.xml
// R.string.title_all_files = "All Files"
// R.string.action_back = "Back"
