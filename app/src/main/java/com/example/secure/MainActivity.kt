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
// import androidx.activity.result.ActivityResultLauncher // No longer directly used here for FAB
// import androidx.activity.result.contract.ActivityResultContracts // No longer directly used here for FAB
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.secure.file.FileManager
import com.example.secure.ui.allfiles.AllFilesScreen
// Import new screens once they are created
// import com.example.secure.ui.viewer.ImageViewerScreen
// import com.example.secure.ui.viewer.VideoPlayerScreen
import com.example.secure.ui.dashboard.MainDashboardScreen
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.text.Charsets

class MainActivity : TrackedActivity() {

    // Permission launchers are mostly for the initial permission check now.
    // File picking and specific actions will be handled within MainDashboardScreen or its ViewModel.

    object NavRoutes {
        const val DASHBOARD = "dashboard"
        const val ALL_FILES = "all_files"
        const val IMAGE_VIEWER = "image_viewer" // Existing, but ensure args are consistent
        const val VIDEO_PLAYER = "video_player" // Existing, but ensure args are consistent
        const val GALLERY_SCREEN = "gallery_screen"

        // Argument names
        const val ARG_ITEM_PATH = "itemPath" // For initially selected item
        const val ARG_INITIAL_INDEX = "initialIndex"
        const val ARG_MEDIA_URIS = "mediaUris" // List of media item URIs for pager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val dashboardViewModel: MainDashboardViewModel = viewModel()
            val navController = rememberNavController()

            ISecureTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = NavRoutes.DASHBOARD) {
                        composable(NavRoutes.DASHBOARD) {
                            MainDashboardScreen(
                                viewModel = dashboardViewModel,
                                onSettingsClick = {
                                    startActivity(Intent(this@MainActivity, com.example.secure.ui.settings.SettingsActivity::class.java))
                                },
                                onCategoryClick = { categoryId ->
                                    Log.d("MainActivity", "Category clicked: $categoryId")
                                     when (categoryId) {
                                         "all_files" -> navController.navigate(NavRoutes.ALL_FILES)
                                         "gallery" -> navController.navigate(NavRoutes.GALLERY_SCREEN)
                                         else -> {
                                             // Handle other category clicks if necessary
                                             Toast.makeText(this@MainActivity, "Category Clicked: $categoryId", Toast.LENGTH_SHORT).show()
                                         }
                                    }
                                }
                            )
                        }
                        composable(NavRoutes.ALL_FILES) {
                            AllFilesScreen(
                                viewModel = dashboardViewModel,
                                navController = navController,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onFileClick = { file -> dashboardViewModel.onFileClicked(file) }
                            )
                        }
                        composable(
                            route = "${NavRoutes.IMAGE_VIEWER}/{${NavRoutes.ARG_INITIAL_INDEX}}?${NavRoutes.ARG_MEDIA_URIS}={${NavRoutes.ARG_MEDIA_URIS}}",
                            arguments = listOf(
                                navArgument(NavRoutes.ARG_INITIAL_INDEX) { type = NavType.IntType },
                                navArgument(NavRoutes.ARG_MEDIA_URIS) { type = NavType.StringType; nullable = false }
                            )
                        ) { backStackEntry ->
                            val initialIndex = backStackEntry.arguments?.getInt(NavRoutes.ARG_INITIAL_INDEX) ?: 0
                            val mediaUrisString = backStackEntry.arguments?.getString(NavRoutes.ARG_MEDIA_URIS) ?: ""
                            val mediaUris = mediaUrisString.split(",").map { URLDecoder.decode(it, Charsets.UTF_8.name()) }
                            com.example.secure.ui.viewer.ImageViewerScreen(
                                navController = navController,
                                imageUris = mediaUris,
                                initialIndex = initialIndex
                            )
                        }
                        composable(
                            route = "${NavRoutes.VIDEO_PLAYER}/{${NavRoutes.ARG_INITIAL_INDEX}}?${NavRoutes.ARG_MEDIA_URIS}={${NavRoutes.ARG_MEDIA_URIS}}",
                            arguments = listOf(
                                navArgument(NavRoutes.ARG_INITIAL_INDEX) { type = NavType.IntType },
                                navArgument(NavRoutes.ARG_MEDIA_URIS) { type = NavType.StringType; nullable = false }
                            )
                        ) { backStackEntry ->
                            val initialIndex = backStackEntry.arguments?.getInt(NavRoutes.ARG_INITIAL_INDEX) ?: 0
                            val mediaUrisString = backStackEntry.arguments?.getString(NavRoutes.ARG_MEDIA_URIS) ?: ""
                            val mediaUris = mediaUrisString.split(",").map { URLDecoder.decode(it, Charsets.UTF_8.name()) }
                            com.example.secure.ui.viewer.VideoPlayerScreen(
                                navController = navController,
                                videoUris = mediaUris,
                                initialIndex = initialIndex
                            )
                        }
                        composable(NavRoutes.GALLERY_SCREEN) {
                            // GalleryScreen(
                            //     navController = navController,
                            //     viewModel = dashboardViewModel // or a dedicated GalleryViewModel
                            // )
                            com.example.secure.ui.gallery.GalleryScreen(
                                navController = navController,
                                viewModel = dashboardViewModel
                            )
                        }
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
