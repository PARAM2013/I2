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
import com.example.secure.databinding.ActivityMainBinding
import com.example.secure.file.FileManager
import android.widget.Toast

class MainActivity : TrackedActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Auto-lock timer related variables
    // companion object {
    //     const val INACTIVITY_TIMEOUT_MS = 90000L // 90 seconds
    // }
    // private var lastInteractionTime: Long = 0L
    // private val inactivityHandler = android.os.Handler(android.os.Looper.getMainLooper())
    // private val inactivityRunnable = Runnable {
    //     // Lock the app: navigate to LockScreenActivity
    //     // You'll need a flag or state management to know the app is locked
    //     // For now, just log or show a toast
    //     // Log.d("MainActivity", "Inactivity timeout reached, should lock app.")
    //     // val intent = Intent(this, LockScreenActivity::class.java)
    //     // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    //     // startActivity(intent)
    //     // finish() // Close MainActivity
    // }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        // This should be called before super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Optional: Keep the splash screen on-screen for longer periods.
        // splashScreen.setKeepOnScreenCondition { true }
        // Optional: Customize the animation for dismissing the splash screen.
        // splashScreen.setOnExitAnimationListener { splashScreenViewProvider -> /* ... */ }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar) // Assuming you add a Toolbar with id 'toolbar' in activity_main.xml

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_secure_dashboard) // Top-level destination
            // drawerLayout = binding.drawerLayout // If you add a DrawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

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

    // Called when returning from the settings screen for MANAGE_EXTERNAL_STORAGE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FileManager.REQUEST_MANAGE_STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission granted.", Toast.LENGTH_SHORT).show()
                    loadVaultContent()
                } else {
                    Toast.makeText(this, "MANAGE_EXTERNAL_STORAGE permission is required to manage files.", Toast.LENGTH_LONG).show()
                    // Handle denial: close app, show error, or disable features.
                    // Forcing a re-check or re-request might lead to loops if user keeps denying.
                    // Consider showing a persistent message or a dedicated screen explaining the need.
                    // For now, we'll attempt to load content, which will likely fail or show empty for the vault.
                    loadVaultContent() // Or show an explicit error state
                }
            }
        }
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
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}
