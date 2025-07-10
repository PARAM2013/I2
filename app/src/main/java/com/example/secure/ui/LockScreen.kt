package com.example.secure.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    onPinCorrect: () -> Unit,
    onFingerprintClick: () -> Unit,
    isFingerprintEnabled: Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    val maxPinLength = 4

    // Using login_animation.json as requested
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.example.secure.R.raw.login_animation))
    val progress by animateLottieCompositionAsState(composition)

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Animation (Top Section)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(0.8f)
                )
            }

            // Authentication Area (Bottom Section)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (enteredPin.isEmpty()) "Create your PIN" else "Enter your PIN",
                    style = MaterialTheme.typography.headlineSmall
                )

                // PIN Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until maxPinLength) {
                        OutlinedBox(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center // This should center its direct child
                        ) {
                            if (i < enteredPin.length) {
                                // The Canvas will be the direct child, let OutlinedBox center it.
                                // Apply the size for the dot directly to Canvas.
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.size(16.dp) // Size of the dot
                                ) {
                                    drawCircle(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Numeric Keypad
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.width(240.dp), // Adjust width as needed
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(9) { number ->
                        TextButton(
                            onClick = {
                                if (enteredPin.length < maxPinLength) {
                                    enteredPin += (number + 1).toString()
                                    if (enteredPin.length == maxPinLength) {
                                        // TODO: Add PIN verification logic
                                        onPinCorrect()
                                    }
                                }
                            },
                            modifier = Modifier.aspectRatio(1f)
                        ) {
                            Text((number + 1).toString(), style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }

                // Fingerprint & Backspace Row
                Row(
                    modifier = Modifier.width(240.dp), // Match keypad width
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFingerprintEnabled) {
                        IconButton(onClick = onFingerprintClick) {
                            Icon(Icons.Default.Fingerprint, contentDescription = "Fingerprint")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp)) // Placeholder for alignment
                    }

                    TextButton(
                        onClick = {
                            if (enteredPin.length < maxPinLength) {
                                enteredPin += "0"
                                if (enteredPin.length == maxPinLength) {
                                    // TODO: Add PIN verification logic
                                    onPinCorrect()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("0", style = MaterialTheme.typography.headlineMedium)
                    }

                    IconButton(onClick = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                        }
                    }) {
                        Icon(Icons.Default.Backspace, contentDescription = "Backspace")
                    }
                }
            }
        }
    }
}

@Composable
fun OutlinedBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.then(
            Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
        ),
        contentAlignment = contentAlignment,
        content = content
    )
}
