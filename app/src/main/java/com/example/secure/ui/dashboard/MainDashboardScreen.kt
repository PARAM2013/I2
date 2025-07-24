package com.example.secure.ui.dashboard

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.secure.R
import com.example.secure.file.FileManager
import com.example.secure.ui.composables.CreateFolderDialog // Import extracted dialog
import com.example.secure.ui.theme.ISecureTheme
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: MainDashboardViewModel = viewModel(),
    onSettingsClick: () -> Unit,
    onCategoryClick: (String) -> Unit // Pass category ID or route
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // FAB and related logic (showFabMenu, filePickerLauncher) are being removed from this screen.
    // They will be added to AllFilesScreen.kt.
    // var showFabMenu by remember { mutableStateOf(false) } // Removed
    // val filePickerLauncher = ... // Removed

    // Effect to show snackbar for file operation results
    LaunchedEffect(uiState.fileOperationResult) {
        uiState.fileOperationResult?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFileOperationResult() // Clear after showing
        }
    }

    // Effect to show snackbar for errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = "Error: $it",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError() // Clear after showing
        }
    }

    ISecureTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.app_name)) },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.title_settings)
                            )
                        }
                    }
                )
            }
            // floatingActionButton parameter and its content removed from Scaffold
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DashboardLottieAnimation(modifier = Modifier.fillMaxWidth().height(150.dp))
                    }

                    if (uiState.categories.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                                Text("Vault is empty. Use the + button to add files or folders.")
                            }
                        }
                    } else {
                        items(uiState.categories, key = { it.id }) { category ->
                            CategoryCard(
                                title = category.title,
                                subtitle = category.subtitle,
                                iconResId = category.iconResId,
                                onClick = { onCategoryClick(category.id) },
                                thumbnail = {
                                    if (category.thumbnail != null) {
                                        Image(
                                            bitmap = category.thumbnail.asImageBitmap(),
                                            contentDescription = category.title,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = category.iconResId),
                                            contentDescription = category.title,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    if (uiState.showCreateFolderDialog) {
        CreateFolderDialog(
            onDismissRequest = { viewModel.requestCreateFolderDialog(false) },
            onConfirm = { folderName ->
                viewModel.createFolder(folderName)
                // ViewModel now handles dismissing the dialog by setting showCreateFolderDialog = false
            }
        )
    }
}

// CreateFolderDialog has been moved to ui/composables/CommonDialogs.kt

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    iconResId: Int,
    onClick: () -> Unit,
    thumbnail: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (thumbnail != null) {
                    thumbnail()
                } else {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = title, // Content description for accessibility
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null, // Decorative
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DashboardLottieAnimation(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val rawId = context.resources.getIdentifier("dashboard_animation", "raw", context.packageName)

    if (rawId != 0) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawId))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.height(150.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("Lottie animation 'dashboard_animation.json' not found in res/raw")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainDashboardScreenPreview() {
    ISecureTheme {
        // For a more useful preview, consider using a fake ViewModel that provides sample UiState
        // For instance:
        // val fakeViewModel = remember {
        //     object : MainDashboardViewModel(Application()) { // Or a more specific fake Application
        //         init {
        //             _uiState.value = MainDashboardUiState(
        //                 categories = listOf(
        //                     DashboardCategoryItem("all", "All Files", "10 files, 2 folders", Icons.Filled.Folder),
        //                     DashboardCategoryItem("img", "Images", "5 files, 10 MB", Icons.Filled.Image)
        //                 ),
        //                 isLoading = false
        //             )
        //         }
        //     }
        // }
        MainDashboardScreen(
            // viewModel = fakeViewModel, // Use the fake ViewModel here
            onSettingsClick = {},
            onCategoryClick = {}
        )
    }
}

// CreateFolderDialogPreview has been moved to ui/composables/CommonDialogs.kt

@Preview(showBackground = true)
@Composable
fun CategoryCardPreview() {
    ISecureTheme {
        CategoryCard(
            title = "Images",
            subtitle = "123 files, 45.6 MB",
            iconResId = R.drawable.ic_image,
            onClick = {}
        )
    }
}
