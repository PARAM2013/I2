package com.example.secure.ui.viewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.SubcomposeAsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImage(
    file: File,
    modifier: Modifier = Modifier,
    pagerState: PagerState? = null // Optional PagerState for integration
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(pagerState?.currentPage) {
        // Reset zoom and offset when page changes
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    offset += pan
                }
            }
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
            }
            .scrollable(
                state = rememberScrollableState { delta ->
                    // Consume the scroll event if the image is zoomed in
                    if (scale > 1f) {
                        offset = Offset(
                            x = (offset.x + delta).coerceIn(
                                -((scale - 1) * size.width / 2),
                                ((scale - 1) * size.width / 2)
                            ),
                            y = (offset.y).coerceIn(
                                -((scale - 1) * size.height / 2),
                                ((scale - 1) * size.height / 2)
                            )
                        )
                    }
                    delta
                },
                orientation = Orientation.Horizontal,
                enabled = pagerState != null && scale > 1f
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

class ZoomableState {
    var scale: Float by mutableFloatStateOf(1f)
    var offsetX: Float by mutableFloatStateOf(0f)
    var offsetY: Float by mutableFloatStateOf(0f)

    fun reset() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }
}

@Composable
fun rememberZoomableState() = remember { ZoomableState() }
