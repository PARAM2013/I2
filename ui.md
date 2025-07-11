## UI Breakdown

# Lock & PIN Screens
Lock & PIN Screens 
This screen serves as the secure gateway to the application, combining a welcoming feel with robust authentication.

Layout:

A Column centered vertically, occupying the full screen.

Welcome Text (Top Section):

animation already added in current, use same.

Authentication Area (Bottom Section):

Prompt Text: A Text composable with style = MaterialTheme.typography.headlineSmall. Text will be "Enter your PIN" or "Create your PIN".

PIN Indicator: A Row of four outlined Box composables that fill in as the user types.

Numeric Keypad: A LazyVerticalGrid with 3 columns for the number pad (1-9).

Fingerprint & Backspace Row: Below the keypad, a Row will contain:

An IconButton on the left with a fingerprint icon (Icons.Default.Fingerprint). This button will trigger the biometric prompt. It should only be visible if fingerprint unlock is enabled and supported.

The "0" key in the center.

An IconButton on the right for backspace.

Component Usage:

Scaffold: For the basic screen structure.

Column/Row: For vertical and horizontal arrangement.

Text: For prompts.

Icon/IconButton: For the fingerprint and backspace actions.

TextButton: For the numeric keys.

User Interaction:

On app open, 

The user can either tap the fingerprint icon to authenticate instantly or start typing their PIN.

PIN indicators update in real-time as the user types.

Once four digits are entered, the app automatically processes the PIN.





# Main Dashboard Screen
Layout:

Scaffold: The root component.

TopAppBar:

Title: "iSecure".

Actions: An IconButton using Icons.Default.MoreVert to open a menu leading to the Settings screen.

Category Display Area:

A LazyColumn containing four distinct, clickable Card composables, one for each category.

Each card will contain a Row with:

An Icon on the left representing the category (e.g., Icons.Default.Folder, Icons.Default.Image).

A Column in the center with a main Text for the category title ("All Files", "Images", etc.) and a subtitle Text for details ("12 folders, 42 files" or "15 files, 75 MB").

An Icon on the right (Icons.Default.ChevronRight) to indicate it's clickable.

FloatingActionButton (FAB):

A standard FloatingActionButton with a + icon (Icons.Default.Add) for importing new files or creating folders. This can be a Multi-Directional FAB as described in earlier versions if desired.

User Interaction & Navigation:

All Files Card: Navigates to a screen showing the root folder structure, allowing the user to browse through folders. This view will use the FileListItem.kt composable.

Images Card: Navigates to a dedicated screen displaying a grid (LazyVerticalGrid) of all image files, sorted from newest to oldest.

Videos Card: Navigates to a screen showing a list or grid of all video files, sorted from newest to oldest.

Documents Card: Navigates to a screen showing a list (LazyColumn) of all other file types (PDFs, DOCX, TXT, etc.), sorted from newest to oldest.

FileListItem.kt Composable (Used on subsequent file list/grid screens):

Root: A Material 3 Card with a subtle elevation and Modifier.combinedClickable.

Content (List View): A Row containing an Icon for the file type, a Text for the filename, and a three-dots IconButton for a context menu.

Content (Grid View): A Column containing a large Icon for the file type and a Text for the filename below it.

DropdownMenu (Context Menu): Triggered by the three-dots icon or a long press, containing DropdownMenuItems for "View", "Rename", "move", "Unhide", and "Delete".

In below part
add this app\src\main\res\raw\dashboard_animation.json (animation lottiefiles json)

when file import , show like progress bar.

# Settings Screen

A LazyColumn to display the list of settings. This ensures the screen is scrollable if more options are added later.

Setting Items (Composable Rows):

Change PIN:

A Row with an Icon (Icons.Default.Password), a Text ("Change PIN")

Enable Fingerprint Unlock:

A Row with an Icon (Icons.Default.Fingerprint) and a Text ("Use Fingerprint").

On the far right, a Material 3 Switch component to toggle the feature on/off. The row itself will also be clickable to toggle the switch.

Remove Metadata on Import:

A Row with an Icon (Icons.Default.PhotoFilter) and a Text ("Strip file metadata").

A Switch on the right to control this setting.

A smaller Text composable below the main label explaining: "Removes location and other EXIF data from photos and videos upon import."

After End of Screen(Footer) set text msg like this "This app is make by KING"