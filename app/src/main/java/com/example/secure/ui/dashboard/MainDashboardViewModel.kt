package com.example.secure.ui.dashboard

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secure.file.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast

data class DashboardCategoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
    // onClick is handled by the screen based on id/route
)

class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _categories = MutableStateFlow<List<DashboardCategoryItem>>(emptyList())
    val categories: StateFlow<List<DashboardCategoryItem>> = _categories.asStateFlow()

    private val fileManager = FileManager // Re-introduce FileManager
    private val app = application // For context if needed by FileManager or for Toasts

    init {
        loadDashboardCategories()
    }

    private fun loadDashboardCategories() {
        viewModelScope.launch {
            // For now, using placeholder data.
            // TODO: Later, update subtitles by fetching data from FileManager (e.g., counts, sizes)
            val placeholderCategories = listOf(
                DashboardCategoryItem(
                    id = "all_files",
                    title = "All Files",
                    subtitle = "Browse all files and folders", // Updated placeholder
                    icon = Icons.Filled.Folder
                ),
                DashboardCategoryItem(
                    id = "images",
                    title = "Images",
                    subtitle = "View your images", // Updated placeholder
                    icon = Icons.Filled.Image
                ),
                DashboardCategoryItem(
                    id = "videos",
                    title = "Videos",
                    subtitle = "Watch your videos", // Updated placeholder
                    icon = Icons.Filled.Videocam
                ),
                DashboardCategoryItem(
                    id = "documents",
                    title = "Documents",
                    subtitle = "Read your documents", // Updated placeholder
                    icon = Icons.Filled.Article
                )
            )
            _categories.value = placeholderCategories
        }
    }

    fun createFolderInCurrentPath(folderName: String) {
        viewModelScope.launch {
            // For the main dashboard, "current path" is always the vault root for creating new top-level folders.
            // If folder creation was context-dependent (e.g., inside another folder),
            // this ViewModel would need to manage a currentPath state.
            // For now, we assume creation is at the root of the vault.
            Log.d("MainDashboardViewModel", "Attempting to create folder: $folderName at vault root.")
            val newFolder = fileManager.createSubFolderInVault(folderName, null) // null for root path
            if (newFolder != null) {
                Log.i("MainDashboardViewModel", "Folder '$folderName' created successfully at ${newFolder.absolutePath}")
                // Optionally, show a success message to the user via a StateFlow event or similar
                // For now, the Toast is in the Composable.
                // If categories needed updating (e.g., 'All Files' subtitle), call loadDashboardCategories() or a specific update method.
            } else {
                Log.e("MainDashboardViewModel", "Failed to create folder '$folderName' at vault root.")
                // Optionally, show an error message
                Toast.makeText(app, "Failed to create folder '$folderName'", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Future methods could update subtitles based on actual file stats
    // fun updateCategoryStats() { ... }
}
