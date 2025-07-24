package com.example.secure.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun ZoomableImage(
    file: File,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val scope = rememberCoroutineScope()
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scope.launch {
            scale.snapTo(scale.value * zoomChange)
            val newOffsetX = offsetX.value + offsetChange.x
            val newOffsetY = offsetY.value + offsetChange.y
            val maxX = (scale.value - 1) * size.width / 2
            val maxY = (scale.value - 1) * size.height / 2
            offsetX.snapTo(newOffsetX.coerceIn(-maxX, maxX))
            offsetY.snapTo(newOffsetY.coerceIn(-maxY, maxY))
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        scope.launch {
                            if (scale.value > 1f) {
                                scale.animateTo(1f)
                                offsetX.animateTo(0f)
                                offsetY.animateTo(0f)
                            }
                        }
                    },
                    onDoubleTap = { tapOffset ->
                        scope.launch {
                            if (scale.value > 1f) {
                                scale.animateTo(1f)
                                offsetX.animateTo(0f)
                                offsetY.animateTo(0f)
                            } else {
                                scale.animateTo(3f)
                                val newOffsetX = (tapOffset.x - size.width / 2) * 2
                                val newOffsetY = (tapOffset.y - size.height / 2) * 2
                                offsetX.animateTo(-newOffsetX)
                                offsetY.animateTo(-newOffsetY)
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
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = offsetX.value,
                    translationY = offsetY.value
                )
        )
    }
}
