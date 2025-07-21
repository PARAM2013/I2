package com.example.secure.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun ZoomableImage(
    file: File,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val scope = rememberCoroutineScope()
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            if (scale > 1f) {
                                animate(scale, 1f) { value, _ -> scale = value }
                                animate(offset.x, 0f) { value, _ -> offset = Offset(value, offset.y) }
                                animate(offset.y, 0f) { value, _ -> offset = Offset(offset.x, value) }
                            } else {
                                animate(scale, 3f) { value, _ -> scale = value }
                            }
                        }
                    }
                )
            }
            .transformable(state = state)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = file),
            contentDescription = "Zoomable Image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}
