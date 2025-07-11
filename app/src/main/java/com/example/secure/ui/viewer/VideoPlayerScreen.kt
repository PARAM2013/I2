package com.example.secure.ui.viewer

import android.view.ViewGroup
import android.widget.FrameLayout
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.secure.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    videoUris: List<String>,
    initialIndex: Int
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex)
    var currentPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    fun initializePlayer(pageIndex: Int) {
        currentPlayer?.release() // Release previous player instance
        if (videoUris.isNotEmpty() && pageIndex < videoUris.size) {
            val newPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUris[pageIndex]))
                prepare()
                playWhenReady = true // Autoplay
            }
            currentPlayer = newPlayer
        }
    }

    // Initialize player for the initial page
    LaunchedEffect(Unit) { // Use Unit to run only once on initial composition
        if (videoUris.isNotEmpty()) {
            initializePlayer(initialIndex)
        }
    }

    // Observe page changes to switch video
    LaunchedEffect(pagerState.currentPage, videoUris) {
        if (videoUris.isNotEmpty()) {
            initializePlayer(pagerState.currentPage)
        }
    }

    // Lifecycle management for the player
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> currentPlayer?.pause()
                Lifecycle.Event.ON_RESUME -> currentPlayer?.play()
                Lifecycle.Event.ON_DESTROY -> {
                    currentPlayer?.release()
                    currentPlayer = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentPlayer?.release()
            currentPlayer = null
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_video_player) + " (${pagerState.currentPage + 1}/${videoUris.size})",
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
            if (videoUris.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_videos_to_display)) // TODO: Add string R.string.no_videos_to_display
                }
                return@Scaffold
            }

            HorizontalPager(
                count = videoUris.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page -> // page here is the index of the current page
                // The AndroidView will be recomposed when pagerState.currentPage changes if it's keyed correctly,
                // but the player logic is handled by LaunchedEffect(pagerState.currentPage)
                // We only want one PlayerView active at a time, associated with `currentPlayer`
                Box(modifier = Modifier.fillMaxSize()) {
                    if (page == pagerState.currentPage && currentPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = currentPlayer
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    // Hide controller initially or use default behavior
                                    // controllerAutoShow = true
                                    // controllerHideOnTouch = true
                                }
                            },
                            update = { view ->
                                // This ensures the PlayerView instance is updated if the player changes
                                // though in this setup, the player instance is managed outside and set in factory.
                                // If the player instance itself changes for the *same* page (which it shouldn't here),
                                // this update block would be crucial.
                                view.player = currentPlayer
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder for non-active pages to ensure smooth swipe
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
