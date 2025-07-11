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
import com.example.secure.file.FileManager // Will be used later for actual data
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    // private val fileManager = FileManager // To be used later
    // private val context = application.applicationContext // To be used later

    init {
        loadDashboardCategories()
    }

    private fun loadDashboardCategories() {
        viewModelScope.launch {
            // In a real app, you would fetch data from FileManager or a repository
            // For example, count files in each category, total sizes, etc.
            // For now, using placeholder data.
            val placeholderCategories = listOf(
                DashboardCategoryItem(
                    id = "all_files",
                    title = "All Files",
                    subtitle = "0 folders, 0 files", // Placeholder
                    icon = Icons.Filled.Folder
                ),
                DashboardCategoryItem(
                    id = "images",
                    title = "Images",
                    subtitle = "0 files, 0 MB", // Placeholder
                    icon = Icons.Filled.Image
                ),
                DashboardCategoryItem(
                    id = "videos",
                    title = "Videos",
                    subtitle = "0 files, 0 MB", // Placeholder
                    icon = Icons.Filled.Videocam
                ),
                DashboardCategoryItem(
                    id = "documents",
                    title = "Documents",
                    subtitle = "0 files, 0 MB", // Placeholder
                    icon = Icons.Filled.Article
                )
            )
            _categories.value = placeholderCategories
        }
    }

    // Future methods could update subtitles based on actual file stats
    // fun updateCategoryStats() { ... }
}
