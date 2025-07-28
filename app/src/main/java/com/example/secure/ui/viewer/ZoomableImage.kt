package com.example.secure.ui.viewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImage(
    file: File,
    modifier: Modifier = Modifier,
    pagerState: PagerState? = null, // Optional PagerState for integration
    onScaleChange: (Float) -> Unit // Callback to report scale changes
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val coroutineScope = rememberCoroutineScope()

    // Report scale changes
    LaunchedEffect(scale) {
        onScaleChange(scale)
    }

    // Reset zoom and offset when page changes
    LaunchedEffect(pagerState?.currentPage) {
        scale = 1f
        offset = Offset.Zero
    }

    val state = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        val newOffset = offset + panChange

        // Calculate the bounds of the scaled image
        val imageWidth = size.width * newScale
        val imageHeight = size.height * newScale

        val maxX = (imageWidth - size.width).coerceAtLeast(0f) / 2f
        val maxY = (imageHeight - size.height).coerceAtLeast(0f) / 2f

        // Coerce the offset to stay within the bounds
        offset = Offset(
            x = newOffset.x.coerceIn(-maxX, maxX),
            y = newOffset.y.coerceIn(-maxY, maxY)
        )
        scale = newScale
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .transformable(state = state)
            .pointerInput(Unit) {
                detectDragGestures {
                    change, dragAmount ->
                    if (scale > 1f) {
                        // When zoomed in, pan the image
                        val imageWidth = size.width * scale
                        val imageHeight = size.height * scale

                        val maxX = (imageWidth - size.width).coerceAtLeast(0f) / 2f
                        val maxY = (imageHeight - size.height).coerceAtLeast(0f) / 2f

                        offset = Offset(
                            x = (offset.x + dragAmount.x).coerceIn(-maxX, maxX),
                            y = (offset.y + dragAmount.y).coerceIn(-maxY, maxY)
                        )
                    } else {
                        // When not zoomed, allow horizontal swipe for pager
                        if (pagerState != null) {
                            coroutineScope.launch {
                                if (dragAmount.x < 0) {
                                    pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(pagerState.pageCount - 1))
                                } else {
                                    pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                                }
                            }
                        }
                    }
                }
            }
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = file,
            contentDescription = "Zoomable image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}