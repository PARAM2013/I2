package com.example.secure

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.secure.file.FileManager
import com.example.secure.ui.dashboard.MainDashboardScreen
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.files.FileBrowserScreen // Import the new screen
import com.example.secure.ui.theme.ISecureTheme

// Define an enum or sealed class for screen states if navigation becomes more complex
enum class CurrentScreen {
    DASHBOARD,
    FILE_BROWSER
    // Add other screens like IMAGE_VIEWER, VIDEO_PLAYER, DOCUMENT_VIEWER, SETTINGS etc.
}

class MainActivity : TrackedActivity() {

    // ... other properties ...

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember { mutableStateOf(CurrentScreen.DASHBOARD) }
            val dashboardViewModel: MainDashboardViewModel = viewModel()

            ISecureTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    when (currentScreen) {
                        CurrentScreen.DASHBOARD -> {
                            MainDashboardScreen(
                                viewModel = dashboardViewModel,
                                onSettingsClick = {
                                    startActivity(Intent(this, com.example.secure.ui.settings.SettingsActivity::class.java))
                                },
                                onCategoryClick = { categoryId ->
                                    if (categoryId == "all_files") {
                                        currentScreen = CurrentScreen.FILE_BROWSER
                                    } else {
                                        Toast.makeText(this, "Category Clicked: $categoryId (Navigation TBD)", Toast.LENGTH_SHORT).show()
                                        Log.d("MainActivity", "Category clicked: $categoryId")
                                    }
                                },
                                onFabClick = {
                                    Toast.makeText(this, "FAB Clicked - TODO: Show options", Toast.LENGTH_LONG).show()
                                    Log.d("MainActivity", "FAB Clicked - Placeholder action")
                                }
                            )
                        }
                        CurrentScreen.FILE_BROWSER -> {
                            FileBrowserScreen(
                                onNavigateUp = {
                                    currentScreen = CurrentScreen.DASHBOARD // Navigate back to dashboard
                                }
                            )
                        }
                        // Add cases for other screens as they are implemented
                    }
                }
            }
        }
        checkAndRequestPermissions()
    }

    private fun setupPermissionLaunchers() {
        // manageStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        //         if (Environment.isExternalStorageManager()) {
        //             Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission granted.", Toast.LENGTH_SHORT).show()
        //         } else {
        //             Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission is required.", Toast.LENGTH_LONG).show()
        //         }
        //         // Potentially refresh ViewModel data if needed after permission change
        //         // val viewModel: MainDashboardViewModel by viewModels()
        //         // viewModel.refreshCategories()
        //     }
        // }

        // pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        //     uri?.let { selectedUri ->
        //         Toast.makeText(this, "Selected file: ${selectedUri.lastPathSegment}", Toast.LENGTH_SHORT).show()
        //         // val viewModel: MainDashboardViewModel by viewModels()
        //         // viewModel.importFile(selectedUri) // Assuming ViewModel has importFile
        //     } ?: Toast.makeText(this, "File selection cancelled.", Toast.LENGTH_SHORT).show()
        // }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // For Android 11+
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    // manageStoragePermissionLauncher.launch(intent) // Use if launcher is active
                    startActivityForResult(intent, FileManager.REQUEST_MANAGE_STORAGE_PERMISSION_CODE) // Fallback if launcher not used here
                    Toast.makeText(this, "Requesting MANAGE_EXTERNAL_STORAGE permission.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    // manageStoragePermissionLauncher.launch(intent) // Use if launcher is active
                    startActivityForResult(intent, FileManager.REQUEST_MANAGE_STORAGE_PERMISSION_CODE) // Fallback
                    Toast.makeText(this, "Requesting MANAGE_EXTERNAL_STORAGE permission (generic).", Toast.LENGTH_LONG).show()
                }
            } else {
                // MANAGE_EXTERNAL_STORAGE already granted
                loadVaultContentInitial()
            }
        } else { // For Android 10 and below
            if (!FileManager.checkStoragePermissions(this)) {
                FileManager.requestStoragePermissions(this)
            } else {
                loadVaultContentInitial()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FileManager.REQUEST_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Storage permissions granted.", Toast.LENGTH_SHORT).show()
                loadVaultContentInitial()
            } else {
                Toast.makeText(this, "Storage permissions are required.", Toast.LENGTH_LONG).show()
                // Handle permission denial - ViewModel/UI should ideally reflect this state
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileManager.REQUEST_MANAGE_STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission granted.", Toast.LENGTH_SHORT).show()
                    loadVaultContentInitial()
                } else {
                    Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission is required.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadVaultContentInitial() {
        // This function is a placeholder. The actual loading of dashboard content (categories)
        // is handled by MainDashboardViewModel's init block.
        // If category subtitles (file counts, sizes) need to be dynamic and depend on permissions,
        // the ViewModel would need a method to refresh its data, which could be called from here.
        Log.d("MainActivity", "Permissions are granted. MainDashboardViewModel will load its data.")
        // Example: if ViewModel needs explicit refresh:
        // val viewModel: MainDashboardViewModel by viewModels()
        // viewModel.refreshCategoryStatistics()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Not used with current Compose setup in MainActivity
        return super.onSupportNavigateUp()
    }
}
