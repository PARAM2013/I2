package com.example.secure.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.secure.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun ImageViewerScreen(
    navController: NavController,
    imageUris: List<String>,
    initialIndex: Int
) {
    val pagerState = rememberPagerState(initialPage = initialIndex)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // For resource access if needed

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Optionally, display current image index or name
                    Text(
                        text = stringResource(R.string.title_image_viewer) + " (${pagerState.currentPage + 1}/${imageUris.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (imageUris.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_images_to_display)) // TODO: Add string R.string.no_images_to_display
                }
                return@Scaffold
            }

            HorizontalPager(
                count = imageUris.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = imageUris[page],
                    contentDescription = stringResource(R.string.image_viewer_content_desc, page + 1), // TODO: Add string R.string.image_viewer_content_desc
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit, // Fit ensures the whole image is visible
                    placeholder = painterResource(id = R.drawable.ic_placeholder_image),
                    error = painterResource(id = R.drawable.ic_error_image)
                )
            }
        }
    }
}
