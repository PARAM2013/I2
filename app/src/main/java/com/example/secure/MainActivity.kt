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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.secure.file.FileManager
import com.example.secure.ui.allfiles.AllFilesScreen
import com.example.secure.ui.allfiles.ImagesScreen
import com.example.secure.ui.allfiles.VideosScreen
import com.example.secure.ui.allfiles.DocumentsScreen
import com.example.secure.ui.dashboard.MainDashboardScreen
import com.example.secure.ui.dashboard.MainDashboardViewModel
import com.example.secure.ui.theme.ISecureTheme
import com.example.secure.ui.viewer.MediaViewerScreen
import java.io.File

class MainActivity : TrackedActivity() {

    object NavRoutes {
        const val DASHBOARD = "dashboard"
        const val ALL_FILES = "all_files"
        const val IMAGES = "images"
        const val VIDEOS = "videos"
        const val DOCUMENTS = "documents"
        const val MEDIA_VIEWER = "media_viewer/{initialIndex}"

        fun mediaViewer(initialIndex: Int) = "media_viewer/$initialIndex"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val dashboardViewModel: MainDashboardViewModel = viewModel()
            val navController = rememberNavController()
            val uiState by dashboardViewModel.uiState.collectAsState()

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
                                    when (categoryId) {
                                        "all_files" -> navController.navigate(NavRoutes.ALL_FILES)
                                        "images" -> navController.navigate(NavRoutes.IMAGES)
                                        "videos" -> navController.navigate(NavRoutes.VIDEOS)
                                        "documents" -> navController.navigate(NavRoutes.DOCUMENTS)
                                        else -> Toast.makeText(this@MainActivity, "Not Implemented", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        composable(NavRoutes.ALL_FILES) {
                            AllFilesScreen(
                                viewModel = dashboardViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.IMAGES) {
                            ImagesScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onImageClick = { index ->
                                    navController.navigate(NavRoutes.mediaViewer(index))
                                },
                                viewModel = dashboardViewModel
                            )
                        }
                        composable(NavRoutes.VIDEOS) {
                            VideosScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onVideoClick = { index ->
                                    navController.navigate(NavRoutes.mediaViewer(index))
                                },
                                viewModel = dashboardViewModel
                            )
                        }
                        composable(NavRoutes.DOCUMENTS) {
                            DocumentsScreen(
                                viewModel = dashboardViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = NavRoutes.MEDIA_VIEWER,
                            arguments = listOf(navArgument("initialIndex") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
                            val allMediaFiles = (uiState.imageFiles + uiState.videoFiles).sortedByDescending { it.file.lastModified() }.map { it.file }

                            MediaViewerScreen(
                                files = allMediaFiles,
                                initialIndex = initialIndex,
                                onClose = { navController.popBackStack() },
                                onDelete = { file ->
                                    dashboardViewModel.requestDeleteItem(uiState.allFiles.find { it.file == file }!!)
                                    navController.popBackStack()
                                },
                                onUnhide = { file ->
                                    dashboardViewModel.requestUnhideItem(uiState.allFiles.find { it.file == file }!!)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // For Android 11+
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityForResult(intent, FileManager.REQUEST_MANAGE_STORAGE_PERMISSION_CODE)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, FileManager.REQUEST_MANAGE_STORAGE_PERMISSION_CODE)
                }
            } else {
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
                FileManager.clearPermissionCache()
                loadVaultContentInitial()
            } else {
                Toast.makeText(this, "Storage permissions are required.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileManager.REQUEST_MANAGE_STORAGE_PERMISSION_CODE) {
            FileManager.clearPermissionCache()
            if (FileManager.checkStoragePermissions(this)) {
                loadVaultContentInitial()
            } else {
                Toast.makeText(this, "Storage permissions are required.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun loadVaultContentInitial() {
        Log.d("MainActivity", "Permissions granted. ViewModel will load data.")
        // val viewModel: MainDashboardViewModel by viewModels()
        // viewModel.refreshCategoryStatistics()
    }
}
