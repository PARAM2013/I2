# Media Viewer Issues & Solutions Documentation


## 1. Current Issues Overview

### 1.1 Image Viewer Issues
- Only one image is visible during left/right navigation
- Zoom functionality needs improvement
- Navigation between images is not smooth

### 1.2 Video Player Issues
- Previous/Next navigation not working
- Missing additional playback controls
- Video state not properly managed between transitions

## 2. Detailed Analysis

### 2.1 Code Structure Issues

```kotlin
// Current Implementation in MediaViewerScreen.kt
HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize()
) { page ->
    val file = files[page]
    // Missing key parameter causing state management issues
    // Missing proper state restoration between pages
}
```

**Problem:** The current implementation doesn't maintain proper state between page transitions, causing images to disappear during navigation.

### 2.2 Video Player Implementation Issues

```kotlin
// Current Implementation in VideoPlayer.kt
AndroidView(
    factory = { ctx ->
        PlayerView(ctx).also { playerView ->
            playerManager.setupPlayerView(playerView, file, showControls)
        }
    },
    modifier = modifier
)
// Missing custom controls
// Missing navigation controls
// Missing playback speed controls
```

## 3. Solutions

### 3.1 Image Viewer Solutions

#### A. Proper State Management
```kotlin
HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize(),
    key = { files[it].absolutePath } // Add key for state management
) { page ->
    val file = files[page]
    when {
        file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif") -> {
            ZoomableImage(
                file = file,
                modifier = Modifier.fillMaxSize()
            )
        }
        // ... rest of the code
    }
}
```

#### B. Navigation Controls
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.Center)
        .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    if (pagerState.currentPage > 0) {
        IconButton(
            onClick = { 
                scope.launch { 
                    pagerState.animateScrollToPage(pagerState.currentPage - 1) 
                }
            }
        ) {
            Icon(Icons.Default.NavigateBefore, "Previous", tint = Color.White)
        }
    }
    // Next button implementation
}
```

### 3.2 Video Player Solutions

#### A. Enhanced Video Controls
```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(Color.Black.copy(alpha = 0.5f))
        .padding(16.dp)
) {
    // Playback controls implementation
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White)
        }
        // Play/Pause button
        // Next button
        // Speed control
    }
}
```

#### B. Playback Speed Control
```kotlin
// Speed control implementation
if (showSpeedControls) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(0.5f, 1f, 1.5f, 2f).forEach { speed ->
            TextButton(
                onClick = {
                    playbackSpeed = speed
                    playerManager.setPlaybackSpeed(speed)
                    showSpeedControls = false
                }
            ) {
                Text("${speed}x", color = Color.White)
            }
        }
    }
}
```

## 4. Implementation Steps

### 4.1 Required Changes in Files

1. **MediaViewerScreen.kt**
   - Add proper key parameter to HorizontalPager
   - Implement navigation controls
   - Add state management for current file

2. **VideoPlayer.kt**
   - Implement custom control overlay
   - Add playback speed control
   - Add navigation controls
   - Implement proper cleanup

3. **PlayerManager.kt**
   - Add playback speed control
   - Improve player state management
   - Add proper resource cleanup

### 4.2 Required Dependencies

```gradle
dependencies {
    // ExoPlayer dependencies
    implementation "androidx.media3:media3-exoplayer:1.1.1"
    implementation "androidx.media3:media3-ui:1.1.1"
    
    // Compose dependencies
    implementation "androidx.compose.foundation:foundation:1.5.0"
    implementation "com.google.accompanist:accompanist-pager:0.32.0"
}
```

## 5. Testing Checklist

### 5.1 Image Viewer Tests
- [ ] Image navigation (left/right) works smoothly
- [ ] Zoom in/out functions properly
- [ ] State is maintained during navigation
- [ ] Image quality is preserved
- [ ] Navigation controls are visible and functional

### 5.2 Video Player Tests
- [ ] Video playback works smoothly
- [ ] Previous/Next navigation works
- [ ] Playback speed control functions properly
- [ ] Controls auto-hide and show correctly
- [ ] Video state is maintained during navigation
- [ ] Resource cleanup works properly

## 6. Future Enhancements

1. Add gesture support for video player controls
2. Implement picture-in-picture mode for videos
3. Add thumbnail preview for video seeking
4. Implement double-tap to zoom for images
5. Add image rotation support

## 7. Notes for Developers

1. Always test the video player with different file formats
2. Ensure proper cleanup of ExoPlayer resources
3. Test memory usage during rapid navigation
4. Verify touch events don't conflict between controls
5. Test on different screen sizes and orientations

## 8. Security Considerations

1. Ensure media files are accessed securely
2. Implement proper file URI handling
3. Clear media player cache after use
4. Handle file permissions appropriately
5. Implement secure file deletion when needed

