package com.example.secure.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

// DashboardCategoryItem is defined in MainDashboardViewModel.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: MainDashboardViewModel = viewModel(),
    onSettingsClick: () -> Unit,
    onCategoryClick: (String) -> Unit, // Pass category ID or route
    onFabClick: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()

    ISecureTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.app_name)) }, // "iSecure"
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
                FloatingActionButton(onClick = onFabClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.fab_import_file) // Updated to a more generic or specific string
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Adjusted padding
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DashboardLottieAnimation(modifier = Modifier.fillMaxWidth().height(150.dp))
                }

                if (categories.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No categories loaded or defined.") // Placeholder for empty categories
                        }
                    }
                } else {
                    items(categories, key = { it.id }) { category -> // Use key for better performance
                        CategoryCard(
                            title = category.title,
                            subtitle = category.subtitle,
                            icon = category.icon,
                            onClick = { onCategoryClick(category.id) }
                        )
                    }
                }
                // Placeholder for file import progress bar (to be implemented later)
                // item {
                //     Text("File import progress bar will be here", modifier = Modifier.padding(top = 16.dp))
                // }
            }
        }
    }
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
        shape = MaterialTheme.shapes.medium // Added shape
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
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary // Added tint
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleLarge) // Adjusted style
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) // Adjusted style and color
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
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
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever) // Changed to loop
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
            Text("Lottie Animation (dashboard_animation.json missing)")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainDashboardScreenPreview() {
    ISecureTheme {
        // Provide a preview ViewModel if necessary, or use default which has placeholders
        MainDashboardScreen(
            onSettingsClick = {},
            onCategoryClick = {},
            onFabClick = {}
        )
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
