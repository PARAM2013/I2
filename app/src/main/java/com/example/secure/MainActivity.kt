package com.example.secure

import android.os.Build
import android.os.Bundle
import android.os.Environment // Added for isExternalStorageManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import android.content.pm.PackageManager
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.secure.file.FileManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.secure.ui.theme.ISecureTheme // Import the custom theme
import com.example.secure.ui.dashboard.MainDashboardScreen // Import the new dashboard screen
import com.example.secure.ui.dashboard.MainDashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : TrackedActivity() {

    // private lateinit var binding: ActivityMainBinding // No longer needed with Compose
    // private lateinit var appBarConfiguration: AppBarConfiguration // No longer needed with Compose

    private lateinit var manageStoragePermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickFileLauncher: ActivityResultLauncher<String>
    private lateinit var dashboardViewModel: MainDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        // This should be called before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        manageStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission granted.", Toast.LENGTH_SHORT).show()
                    loadVaultContent()
                } else {
                    Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission is required to manage files.", Toast.LENGTH_LONG).show()
                    loadVaultContent()
                }
            }
        }

        pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { selectedUri ->
                Toast.makeText(this, "Selected file: ${selectedUri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                dashboardViewModel.importFile(selectedUri)
            } ?: Toast.makeText(this, "File selection cancelled.", Toast.LENGTH_SHORT).show()
        }

        setContent {
            dashboardViewModel = viewModel()
            ISecureTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainDashboardScreen(
                        onSettingsClick = {
                            // Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, com.example.secure.ui.settings.SettingsActivity::class.java))
                        },
                        onImportFile = {
                            pickFileLauncher.launch("*/*")
                        },
                        onImportFolder = {
                            Toast.makeText(this, "Import Folder Clicked (TODO)", Toast.LENGTH_SHORT).show()
                            // TODO: Implement folder picker
                        },
                        onCreateFolder = { folderName ->
                            dashboardViewModel.createFolder(folderName)
                        },
                        viewModel = dashboardViewModel
                    )
                }
            }
        }

        // Commenting out existing navigation setup as we are using Compose setContent directly for the dashboard
        /*
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_secure_dashboard) // Top-level destination
            // drawerLayout = binding.drawerLayout // If you add a DrawerLayout
        )
        */

        // Initialize last interaction time for auto-lock is now handled by TrackedActivity

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (FileManager.checkStoragePermissions(this)) {
            // Permissions are granted, proceed to load vault content
            loadVaultContent()
        } else {
            FileManager.requestStoragePermissions(this)
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
                // All permissions granted
                Toast.makeText(this, "Storage permissions granted.", Toast.LENGTH_SHORT).show()
                loadVaultContent()
            } else {
                // Permissions denied. Handle this gracefully (e.g., show an error, explain why they are needed)
                // For now, just show a toast. The app might not function correctly.
                Toast.makeText(this, "Storage permissions are required to manage files.", Toast.LENGTH_LONG).show()
                // Optionally, disable UI components that require storage access or guide user to settings.
                // To keep it simple, we might just try to load content and it might fail or show empty.
                // Or, close the app / show a specific "permissions needed" screen.
                // For now, we'll attempt to load content, which will likely show an empty state if permissions are missing.
                loadVaultContent()
            }
        }
    }

    private fun loadVaultContent() {
        // This is where you would trigger the SecureDashboardFragment to load/refresh its content.
        // For now, just a log or a call to a placeholder method in the fragment.
        // Example: findNavController(R.id.nav_host_fragment_activity_main).currentDestination
        // Or, if SecureDashboardFragment is already the primary, it can listen for an event or check on its own.
        android.util.Log.d("MainActivity", "Permissions granted or handled. Attempting to load vault content.")
        // If your SecureDashboardFragment needs an explicit trigger:
        // val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
        // val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
        // if (currentFragment is SecureDashboardFragment) {
        //     currentFragment.loadFiles()
        // }

        // The SecureDashboardFragment itself can also check permissions in its onResume/onViewCreated
        // and display an appropriate message or trigger loading.
        // For now, this explicit call serves as a placeholder for triggering the load.
        // The fragment will need to access FileManager.getVaultDirectory().
        // Actual data loading will be triggered in SecureDashboardFragment's onViewCreated or a similar lifecycle method.
        // However, we might want to re-check/re-load if permissions are granted *after* initial fragment load.
        // This can be done by finding the fragment and calling a public method on it.
        triggerSecureDashboardRefresh()
    }

    

    private fun triggerSecureDashboardRefresh() {
        // Attempt to find the SecureDashboardFragment and call a public method to refresh/load data
        // This is useful if permissions are granted after the fragment was initially created.
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
            val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
            if (currentFragment is com.example.secure.ui.SecureDashboardFragment) {
                // Add a public method like 'refreshData()' or 'onPermissionsGranted()' to SecureDashboardFragment
                // For now, let's assume its existing loadDashboardData can be called if accessible,
                // or it re-checks permissions in its own onResume.
                // If SecureDashboardFragment.loadDashboardData() is not public, this won't work directly.
                // A more robust way is using LiveData/ViewModel or an event bus.
                 currentFragment.parentFragmentManager.beginTransaction().detach(currentFragment).attach(currentFragment).commitAllowingStateLoss()
                 android.util.Log.d("MainActivity", "Attempting to refresh SecureDashboardFragment by reattach.")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error refreshing dashboard fragment", e)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        // This method is part of the old navigation component setup.
        // If you fully transition to Compose Navigation, this will be removed.
        // For now, it's commented out as the primary UI is Compose.
        /*
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        */
        return super.onSupportNavigateUp()
    }

}
