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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.secure.auth.PinManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.secure.R // Required for R.string.app_name
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

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
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Share App
                item {
                    SettingItem(
                        icon = Icons.Filled.Share,
                        title = "Share App",
                        subtitle = if (com.example.secure.util.ApkBackupUtil.hasBackupApk(context)) 
                            "APK backup available in Downloads" else "Share current APK",
                        onClick = {
                            shareApplication(activityContext) // Using activityContext as it's readily available
                        }
                    )
                    // No divider after the last item
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
    subtitle: String? = null,
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

// Function to handle sharing the application APK
private fun shareApplication(context: Context) { // Context is already imported from android.content.Context
    try {
        val applicationInfo = context.applicationInfo
        val sourceApk = File(applicationInfo.sourceDir)

        // Define a name for the copied APK in cache with version
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        val apkName = "iSecureVault_v${versionName}.apk" // Include version in filename
        val destApk = File(context.cacheDir, apkName)

        // Copy APK to cache directory
        FileInputStream(sourceApk).use { inputStream ->
            FileOutputStream(destApk).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        // Ensure the file is readable by other apps (though FileProvider handles permissions)
        destApk.setReadable(true, false)


        val authority = "${context.packageName}.provider"
        val contentUri = FileProvider.getUriForFile(context, authority, destApk)

        if (contentUri == null) {
            Log.e("ShareApp", "Failed to get content URI for APK.")
            Toast.makeText(context, "Error: Could not prepare app for sharing.", Toast.LENGTH_LONG).show()
            return
        }

        val appName = try { context.getString(R.string.app_name) } catch (e: Exception) { "this app" }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Add a subject and text for a richer share experience
            putExtra(Intent.EXTRA_SUBJECT, "Sharing $appName")
            putExtra(Intent.EXTRA_TEXT, "Check out $appName!")
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share $appName Via")
        context.startActivity(chooserIntent)

    } catch (e: ActivityNotFoundException) {
        Log.e("ShareApp", "ActivityNotFoundException: No app can handle this request.", e)
        Toast.makeText(context, "Cannot find an app to share with.", Toast.LENGTH_LONG).show()
    } catch (e: IOException) {
        Log.e("ShareApp", "IOException during APK copy: ${e.message}", e)
        Toast.makeText(context, "Error preparing app for sharing.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Log.e("ShareApp", "Generic exception during app share: ${e.message}", e)
        Toast.makeText(context, "An unexpected error occurred while sharing.", Toast.LENGTH_LONG).show()
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
