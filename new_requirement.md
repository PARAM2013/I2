# Blueprint: Enhanced Media Viewing and Gallery Feature

This document outlines the requirements and implementation plan for adding enhanced image and video viewing capabilities, a dedicated gallery view, and improved document handling to the Secure Photos application.

## 1. User Requirements

1.  **Thumbnail Display:**
    *   In the `AllFilesScreen` list, image files should display a visual thumbnail instead of a generic icon.
    *   Video files should display a relevant thumbnail (either a generic video icon or a generated frame if feasible quickly) instead of a generic icon.
2.  **Full-Screen Image Viewer:**
    *   Tapping an image thumbnail in any list should open it in a dedicated full-screen viewer within the app.
    *   The viewer must support swiping left/right to navigate to the next/previous image in the current folder/gallery list.
    *   Basic zoom/pan capabilities are desirable but can be a secondary enhancement.
3.  **Full-Screen Video Player:**
    *   Tapping a video thumbnail in any list should open it in a dedicated full-screen player within the app.
    *   The player must support swiping left/right to navigate to the next/previous video in the current folder/gallery list.
    *   Standard playback controls (play/pause, seek, volume) should be available.
4.  **Dedicated Gallery View:**
    *   A new "Gallery" section/screen accessible from the main dashboard.
    *   This screen should display a grid of thumbnails for all image and video files in the vault.
    *   Users should be able to filter the gallery view by "Images Only" or "Videos Only".
    *   Tapping a thumbnail in the gallery should open it in the respective full-screen viewer (image or video) with swiping enabled for items in the gallery's current filtered list.
5.  **Document Handling:**
    *   Tapping a document file (e.g., PDF, DOCX, TXT) in the `AllFilesScreen` list should open it using the system's default application for that file type.

## 2. Implementation Strategy

The implementation will primarily use Jetpack Compose for UI, Jetpack Navigation for screen transitions, Coil for image loading, and ExoPlayer for video playback.

### 2.1. Dependencies (Add to `app/build.gradle.kts` & `gradle/libs.versions.toml`)

*   **Coil:** `io.coil-kt:coil-compose:2.6.0` (or latest) - For asynchronous image loading (thumbnails and full-screen images).
*   **ExoPlayer:**
    *   `androidx.media3:media3-exoplayer:1.3.1` (or latest) - Core ExoPlayer library.
    *   `androidx.media3:media3-ui:1.3.1` (or latest) - Provides UI components for ExoPlayer.
    *   `androidx.media3:media3-common:1.3.1` (or latest)

### 2.2. File Handling & Metadata

*   **`FileManager.kt`:**
    *   Verify/enhance `getMimeType(file: File): String?` to reliably determine MIME types. This is crucial for differentiating between images, videos, and documents.
    *   The existing `FileCategory` enum can continue to be used for broad categorization.

### 2.3. Navigation (Jetpack Navigation Component)

*   New routes will be defined in `MainActivity.NavRoutes`.
*   Arguments will be passed to viewer screens, including a list of all relevant media item URIs (for swiping) and the index of the initially selected item.
    *   Example Route for Image Viewer: `image_viewer/{initialIndex}?mediaUris={mediaUris}`
    *   `mediaUris` will be a comma-separated, URL-encoded string of file URIs.

### 2.4. UI Components (Jetpack Compose)

*   **Thumbnails:**
    *   In `AllFilesScreen.FileItem` and `GalleryScreen`, use `coil.compose.AsyncImage` to display thumbnails.
    *   Provide the `File` object directly to `AsyncImage`'s `data` parameter. Coil can handle loading from `File`.
    *   Use `contentScale = ContentScale.Crop` for uniform thumbnail sizes.
    *   Placeholders and error images should be configured for `AsyncImage`.
*   **Full-Screen Viewers (Image & Video):**
    *   Utilize `androidx.compose.foundation.pager.HorizontalPager` for swipe functionality.
    *   Each page in the pager will host either an `AsyncImage` (for images) or an `AndroidView` wrapping ExoPlayer's `PlayerView` (for videos).
*   **Gallery View (`GalleryScreen.kt`):**
    *   Use `androidx.compose.foundation.lazy.grid.LazyVerticalGrid` to display thumbnails.
    *   Implement filter chips/buttons for "All", "Images", "Videos".

## 3. File Modifications & Creations

### 3.1. Existing File Modifications

*   **`app/build.gradle.kts` & `gradle/libs.versions.toml`:**
    *   Add dependencies listed in section 2.1.
*   **`FileManager.kt`:**
    *   Ensure `getMimeType` is robust. Consider using `MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)` as a fallback or primary method if `URLConnection.guessContentTypeFromName` is insufficient.
*   **`MainActivity.kt`:**
    *   In `NavRoutes`: Add routes for `IMAGE_VIEWER`, `VIDEO_PLAYER`, and `GALLERY_SCREEN`.
    *   In `NavHost`: Add `composable` entries for these new routes, defining arguments (e.g., `initialIndex`, `mediaUris`).
    *   Update `NavHost` in `MainActivity.kt` to pass the `NavController` to `AllFilesScreen` and potentially to `MainDashboardScreen` if gallery navigation originates there.
*   **`MainDashboardViewModel.kt`:**
    *   Add a `NavigationEvent` sealed class to handle navigation triggers as single-shot events (e.g., `NavigateToImageViewer(uris, index)`, `NavigateToVideoPlayer(uris, index)`, `OpenDocument(uri, mimeType)`).
    *   Modify `onFileClicked(clickedFile: FileManager.VaultFile)`:
        *   Determine MIME type.
        *   If image: Prepare list of all image file URIs in the current `vaultStats.allFiles`, find index of `clickedFile`, then emit `NavigateToImageViewer`.
        *   If video: Prepare list of all video file URIs, find index, emit `NavigateToVideoPlayer`.
        *   If document: Get content URI via `FileProvider`, emit `OpenDocument`.
    *   Add functions to prepare data for `GalleryScreen` (e.g., fetching all images, all videos, or both).
    *   Add `onNavigationHandled()` to reset navigation events.
*   **`AllFilesScreen.kt`:**
    *   Accept `NavController` as a parameter.
    *   Observe `NavigationEvent` from the ViewModel. Launch navigation or intents accordingly. Call `viewModel.onNavigationHandled()` afterwards.
    *   Modify `FileItem`'s `leadingContent`:
        *   If `vaultFile.category == PHOTO`: Use `AsyncImage` with `vaultFile.file` as data for thumbnail.
        *   If `vaultFile.category == VIDEO`: Use `AsyncImage` with `vaultFile.file` (Coil can load thumbnails for videos) or a default video icon (`Icons.Filled.Movie`).
        *   Else: Keep `Icons.Filled.Description`.
    *   Update `FileItem`'s `onClick` lambda to call `viewModel.onFileClicked(vaultFile)`.
*   **`MainDashboardScreen.kt`:**
    *   Add a new "Gallery" category card.
    *   Its `onClick` should navigate to the new `GalleryScreen`.
*   **`res/xml/file_paths.xml`:**
    *   Ensure a path like `<external-path name="vault_files" path=".iSecureVault/" />` or a sufficiently broad path like `<external-path name="external_files" path="." />` is present and correctly configured to allow `FileProvider.getUriForFile` to work for files in the `.iSecureVault` directory when opening documents.
    *   The authority string in `FileProvider.getUriForFile` must match the one defined in `AndroidManifest.xml` (e.g., `${applicationId}.provider`).
*   **`AndroidManifest.xml`:**
    *   Verify the `FileProvider` declaration and its `android:authorities`.

### 3.2. New File Creations

*   **`ui/viewer/ImageViewerScreen.kt`:**
    *   **Parameters:** `navController: NavController`, `imageUris: List<String>`, `initialIndex: Int`.
    *   **Content:**
        *   `Scaffold` with a top app bar (title, back button).
        *   `HorizontalPager` (state managed by `rememberPagerState(initialPage = initialIndex)`).
        *   Pager content: `AsyncImage` loading `imageUris[page]`, scaled to fit.
        *   Handle potential errors in image loading.
*   **`ui/viewer/VideoPlayerScreen.kt`:**
    *   **Parameters:** `navController: NavController`, `videoUris: List<String>`, `initialIndex: Int`.
    *   **Content:**
        *   `Scaffold` with a top app bar.
        *   `HorizontalPager`.
        *   Pager content:
            *   `AndroidView` factory to create ExoPlayer's `PlayerView`.
            *   Manage ExoPlayer instance lifecycle (`remember`, `DisposableEffect` for creation and release).
            *   Set media item using `MediaItem.fromUri(videoUris[page])`.
            *   Attach `PlayerView` to the player.
        *   Handle player state (play/pause on lifecycle events of the composable).
*   **`ui/gallery/GalleryScreen.kt`:**
    *   **Parameters:** `navController: NavController`, `viewModel: MainDashboardViewModel` (or a new `GalleryViewModel`).
    *   **Content:**
        *   `Scaffold` with top app bar (title "Gallery", back button).
        *   Filter controls (Chips or Buttons for "All", "Images", "Videos").
        *   `LazyVerticalGrid` displaying thumbnails (`AsyncImage`) of media items.
        *   `onClick` for each thumbnail should call a ViewModel function to navigate to the appropriate viewer (`ImageViewerScreen` or `VideoPlayerScreen`), passing the full list of currently filtered items and the clicked item's index.
*   **(Optional) `ui/gallery/GalleryViewModel.kt`:**
    *   If logic for filtering and preparing gallery data becomes complex, extract it here from `MainDashboardViewModel`.
    *   Manages the state for `GalleryScreen` (filtered list of media, current filter).

## 4. Testing Considerations

*   **Import:** Test importing various image (JPG, PNG, WebP, GIF), video (MP4, MKV, 3GP), and document (PDF, TXT, DOCX) types.
*   **Thumbnail Display:** Verify thumbnails load correctly in `AllFilesScreen` and `GalleryScreen`. Check placeholder/error states.
*   **Image Viewer:**
    *   Test opening images from `AllFilesScreen` and `GalleryScreen`.
    *   Verify swiping navigates through the correct list of images.
    *   Test edge cases (first image, last image).
*   **Video Player:**
    *   Test opening videos.
    *   Verify swiping navigates through the correct list of videos.
    *   Test playback controls.
    *   Verify player lifecycle (releases correctly on screen exit).
*   **Document Opening:**
    *   Test opening different document types. Verify they open in the correct system default app.
    *   Test `FileProvider` URI permissions.
*   **Gallery View:**
    *   Test filtering (All, Images, Videos).
    *   Verify navigation to viewers from gallery thumbnails.
*   **Permissions:** Ensure `MANAGE_EXTERNAL_STORAGE` (or legacy storage permissions) work as expected for accessing files.
*   **Error States:** Test what happens if a file is corrupted or of an unexpected format.
*   **Performance:** Assess UI smoothness, especially in gallery view with many items and during swipe navigation in viewers.

This blueprint provides a comprehensive guide for implementing the desired media features. Each step should be developed and tested iteratively.
