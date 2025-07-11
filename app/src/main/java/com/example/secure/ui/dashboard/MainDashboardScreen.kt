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
import com.example.secure.ui.theme.ISecureTheme

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

    var showFabMenu by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importFiles(uris)
            }
            showFabMenu = false // Close menu after selection or cancellation
        }
    )

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
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.title_settings)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    if (showFabMenu) {
                        SmallFloatingActionButton(
                            onClick = {
                                viewModel.requestCreateFolderDialog(true)
                                showFabMenu = false
                            },
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Filled.CreateNewFolder, stringResource(R.string.fab_create_folder))
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                filePickerLauncher.launch("*/*") // Or specific MIME types
                                // showFabMenu will be set to false in onResult by the launcher
                            },
                            modifier = Modifier.padding(bottom = 16.dp) // Extra padding for main FAB
                        ) {
                            Icon(Icons.Filled.UploadFile, stringResource(R.string.fab_import_file))
                        }
                    }
                    FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
                        Icon(
                            imageVector = if (showFabMenu) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = stringResource(R.string.fab_options_toggle)
                        )
                    }
                }
            }
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
                                icon = category.icon,
                                onClick = { onCategoryClick(category.id) }
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

@Composable
fun CreateFolderDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderNameState by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.create_folder_dialog_title)) },
        text = {
            OutlinedTextField(
                value = folderNameState,
                onValueChange = { folderNameState = it },
                label = { Text(stringResource(R.string.folder_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderNameState.text.isNotBlank()) {
                        onConfirm(folderNameState.text.trim())
                    } else {
                        Toast.makeText(context, context.getString(R.string.folder_name_empty_error), Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(stringResource(R.string.create_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
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
                Icon(
                    imageVector = icon,
                    contentDescription = title, // Content description for accessibility
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
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

@Preview(showBackground = true)
@Composable
fun CreateFolderDialogPreview() {
    ISecureTheme {
        CreateFolderDialog(onDismissRequest = {}, onConfirm = {})
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryCardPreview() {
    ISecureTheme {
        CategoryCard(
            title = "Images",
            subtitle = "123 files, 45.6 MB",
            icon = Icons.Filled.Image,
            onClick = {}
        )
    }
}
