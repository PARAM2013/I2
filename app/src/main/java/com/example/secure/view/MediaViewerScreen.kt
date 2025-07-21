package com.example.secure.view

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewerScreen(
    initialPage: Int,
    mediaUris: List<Uri>,
    onNavigateBack: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { mediaUris.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            key = { mediaUris[it] }
        ) { page ->
            val uri = mediaUris[page]
            val file = File(uri.path!!)
            when {
                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif") -> {
                    ZoomableImage(
                        file = file,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                file.extension.lowercase() in listOf("mp4", "mkv", "webm") -> {
                    VideoPlayer(
                        file = file,
                        onPrevious = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        onNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
