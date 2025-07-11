package com.example.secure.ui.settings

import android.app.Activity
import android.app.Application
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Password // Using filled as Default may not exist for this
import androidx.compose.material.icons.filled.PhotoFilter // Using filled
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.secure.R
import com.example.secure.ui.theme.ISecureTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current.applicationContext as Application)),
    onNavigateToPinSetup: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isFingerprintEnabled by viewModel.isFingerprintEnabled.collectAsState()
    val isMetadataRemovalEnabled by viewModel.isMetadataRemovalEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Change PIN
            item {
                SettingItem(
                    icon = Icons.Filled.Password,
                    title = stringResource(R.string.settings_change_pin),
                    onClick = onNavigateToPinSetup
                )
                Divider()
            }

            // Enable Fingerprint Unlock
            item {
                SettingItemWithSwitch(
                    icon = Icons.Filled.Fingerprint,
                    title = stringResource(R.string.settings_use_fingerprint), // New String
                    checked = isFingerprintEnabled,
                    onCheckedChange = { viewModel.setFingerprintEnabled(it) }
                )
                Divider()
            }

            // Remove Metadata on Import
            item {
                SettingItemWithSwitch(
                    icon = Icons.Filled.PhotoFilter,
                    title = stringResource(R.string.settings_strip_metadata), // New String
                    description = stringResource(R.string.settings_strip_metadata_desc), // New String
                    checked = isMetadataRemovalEnabled,
                    onCheckedChange = { viewModel.setMetadataRemovalEnabled(it) }
                )
                Divider()
            }

            // Footer Text
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "This app is make by KING", // As per ui.md
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
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
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingItemWithSwitch(
    icon: ImageVector,
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp), // Reduced padding for items with switches to make them feel more compact
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                description?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

// Basic ViewModelFactory for passing Application context to SettingsViewModel in Preview
// In a real app with Hilt, this wouldn't be necessary for previews in the same way.
class SettingsViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ISecureTheme {
        // In a real app, you might use a fake ViewModel or Hilt for previews.
        // For this preview to work, it needs an Application instance.
        val context = LocalContext.current
        val factory = SettingsViewModelFactory(context.applicationContext as Application)
        SettingsScreen(
            viewModel = viewModel(factory = factory),
            onNavigateToPinSetup = {},
            onNavigateBack = {}
        )
    }
}
