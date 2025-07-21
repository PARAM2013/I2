# Gallery UI/UX Feedback & Suggestions

## 1. Gallery-like Navigation

**Current:**
- Navigation between images works with arrow click.
- There are three dots (`⋮`) per image for Rename/Delete/Unhide.

**Requested/Recommended:**
- **Swipe Left/Right:** Instead of requiring arrow click, users should be able to swipe left or right on the image preview to move between images (like a standard gallery app).
- **Implementation:** Use `HorizontalPager` from Jetpack Compose, or Accompanist Pager, inside your image viewer dialog/overlay. This should allow full-screen swiping between images/videos.
  - On image tap, open a `MediaViewerScreen` that enables horizontal swiping.
  - Support pinch-to-zoom and pan for images.

---

## 2. Long Press for Options

**Current:**
- Three-dot menu (`⋮`) is always shown for Rename, Delete, Unhide.

**Requested/Recommended:**
- **Hide the 3-dot menu.**
- **Show options (Rename, Delete, Unhide) only on long press** on the image (or file row in list view).
  - On long press, show a bottom sheet or dialog with these actions.
  - This reduces clutter and matches gallery UX expectations.

---

## 3. Additional Suggestions for a Perfect Gallery/Vault UI

### A. Full-Screen Media Viewer

- When a user taps an image/video, open it in a true full-screen viewer with:
  - Swipe left/right to navigate (HorizontalPager)
  - Tap to show/hide overlay controls (filename, date, size, delete, unhide, close)
  - Long-press inside viewer to show actions (Rename, Delete, Unhide)
  - Pinch-to-zoom and double-tap to zoom for images
  - For videos: overlay play/pause, seek, mute, speed controls

### B. Grid & List Consistency

- Make sure both Grid and List view use the same logic for entering the media viewer, and always pass the correct, visible media set for navigation.
- Swiping between files should work regardless of entry view.

### C. Visual Polish

- Use rounded corners and subtle shadow for images and thumbnails.
- When in list view, show a small preview thumbnail for images/videos at the start of the row (already looks good from your screenshot).
- Add a subtle selection/highlight effect when an item is tapped or long-pressed.

### D. Batch Actions (Optional)

- Allow multi-select for delete/unhide by long-pressing and then tapping more items (like Google Photos).

### E. File Info Overlay

- In the full-screen viewer, show overlay info (filename, date, size) at the bottom/top with fade-in/out animation on tap.

### F. Accessibility

- Ensure all images/thumbnails have content descriptions for TalkBack.
- Overlay controls/buttons should have high contrast and be reachable with screen readers.

---

## 4. Example User Flow

1. **Browse:** User sees grid or list of images/videos/folders.
2. **Tap:** Tapping an image/video opens full-screen viewer.
3. **Swipe:** User swipes left/right to view next/previous media.
4. **Long Press:** While browsing or in full-screen, long-press shows Rename/Delete/Unhide actions.
5. **Overlay Controls:** Tap to toggle overlay with file info and action buttons.

---

## 5. Summary Table

| Feature                        | Status      | Recommendation                        |
|--------------------------------|-------------|---------------------------------------|
| Swipe navigation               | Only arrows | Enable swipe left/right everywhere    |
| Three-dot menu                 | Always shown| Show only on long press               |
| Full-screen viewer             | ?           | Use for all media, with overlays      |
| Pinch/Double tap to zoom       | ?           | Support in full-screen for images     |
| Video controls                 | Basic?      | Add overlay controls (seek, mute,...) |
| Consistency grid/list          | Partial     | Same logic for both                   |

---

## 6. Technical Notes

- Use `Modifier.pointerInput` with `detectTapGestures` for long press.
- For swipe: Jetpack Compose `HorizontalPager` (or Accompanist Pager).
- For overlays: `AnimatedVisibility` or similar for fade-in/out.
- For batch actions: Use a selectable state and checkbox overlays.
- For accessibility: Compose's `contentDescription` and test with screen readers.

---

## 7. Example Pseudocode for Long-Press

```kotlin
Box(
    Modifier
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { showActionSheet = true }
            )
        }
) {
    Image(...)
    if (showActionSheet) {
        // Show BottomSheet with Rename, Delete, Unhide
    }
}
```

---

## 8. References

- [Jetpack Compose: HorizontalPager](https://developer.android.com/jetpack/compose/libraries#foundation)
- [Accompanist Pager](https://google.github.io/accompanist/pager/)
- [Android Gallery UX inspiration](https://material.io/components/image-lists)

---

**These steps will make your app feel modern, smooth, and intuitive for users wanting a secure, gallery-like vault experience.**