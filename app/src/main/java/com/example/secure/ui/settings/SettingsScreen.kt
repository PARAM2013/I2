package com.example.secure.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.secure.auth.PinManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateUp: () -> Unit, activityContext: Activity) {
    val context = LocalContext.current

    var fingerprintEnabled by remember {
        mutableStateOf(PinManager.isFingerprintAuthEnabled(context))
    }
    var metadataRemovalEnabled by remember {
        mutableStateOf(PinManager.isMetadataRemovalEnabled(context))
    }

    Scaffold(
        topBar = {
            // TopAppBar is handled by SettingsActivity's supportActionBar
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f), // Takes available space, pushing footer down
                contentPadding = PaddingValues(vertical = 8.dp) // Padding for the top/bottom of the list itself
                // No horizontal padding here; items will manage their own.
            ) {
                // Change PIN
                item {
                    SettingItem(
                        icon = Icons.Filled.Password,
                        title = "Change PIN",
                        onClick = {
                            val intent = Intent(activityContext, PinChangeActivity::class.java)
                            activityContext.startActivity(intent)
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Enable Fingerprint Unlock
                item {
                    SwitchSettingItem(
                        icon = Icons.Filled.Fingerprint,
                        title = "Use Fingerprint",
                        checked = fingerprintEnabled,
                        onCheckedChange = { newCheckedState ->
                            fingerprintEnabled = newCheckedState
                            PinManager.setFingerprintAuthEnabled(context, newCheckedState)
                        }
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Remove Metadata on Import
                item {
                    SwitchSettingItem(
                        icon = Icons.Filled.PhotoFilter,
                        title = "Strip file metadata",
                        subtitle = "Removes location and other EXIF data from photos and videos upon import.",
                        checked = metadataRemovalEnabled,
                        onCheckedChange = { newCheckedState ->
                            metadataRemovalEnabled = newCheckedState
                            PinManager.setMetadataRemovalEnabled(context, newCheckedState)
                        }
                    )
                    // No divider after the last item in the main list
                }
            }

            // Footer
            Text(
                text = "This app is make by KING",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Padding for the footer text
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp), // Item handles its full horizontal and vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp), // Item handles its full horizontal and vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 16.dp) // Padding before the switch itself
        )
    }
}

// Preview code remains commented out as before.
/*
@Preview(showBackground = true)
@Composable
fun DefaultPreviewSettingsScreen() {
    SecureAppTheme {
        // Dummy states for preview
        var previewFingerprintEnabled by remember { mutableStateOf(true) }
        var previewMetadataRemovalEnabled by remember { mutableStateOf(false) }

        Scaffold { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp) // This padding would be for the overall LazyColumn container
            ) {
                item {
                    SettingItem( // These items would then not need horizontal padding if LazyColumn handles it
                        icon = Icons.Filled.Password,
                        title = "Change PIN",
                        onClick = {}
                    )
                    Divider()
                }
                item {
                    SwitchSettingItem(
                        icon = Icons.Filled.Fingerprint,
                        title = "Use Fingerprint",
                        checked = previewFingerprintEnabled,
                        onCheckedChange = { previewFingerprintEnabled = it }
                    )
                    Divider()
                }
                item {
                    SwitchSettingItem(
                        icon = Icons.Filled.PhotoFilter,
                        title = "Strip file metadata",
                        subtitle = "Removes location and other EXIF data from photos and videos upon import.",
                        checked = previewMetadataRemovalEnabled,
                        onCheckedChange = { previewMetadataRemovalEnabled = it }
                    )
                    Divider()
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "This app is make by KING",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
*/
