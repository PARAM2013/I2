Main Dashboard Screen 
This is the user's primary interaction hub for managing files.

Layout:

Scaffold: The root component.

TopAppBar:

Title: "iSecure".

Actions:

An IconButton for toggling between Icons.Default.ViewList (List View) and Icons.Default.ViewModule (Grid View).

An IconButton using Icons.Default.MoreVert to open a menu leading to the Settings screen.

File Display Area:

This area will conditionally display either a LazyColumn or a LazyVerticalGrid based on the user's view preference.

Empty State: If there are no files, this area will display a centered Column with a large Icon (e.g., Icons.Default.FolderOff) and a Text saying "Your vault is empty. Tap the '+' button to add files."

Multi-Directional FloatingActionButton (FAB):

The main FAB will display a + icon (Icons.Default.Add).

When tapped, it will animate (e.g., rotate) and reveal three smaller, labeled FABs in a vertical column above it:

Create Folder (Icon + Text)

Import Folder (Icon + Text)

Import File (Icon + Text)

FileListItem.kt Composable (The items in the list/grid):

Root: A Material 3 Card with a subtle elevation and Modifier.combinedClickable.

Content (List View): A Row containing:

An Icon on the left, dynamically chosen based on file type (e.g., Icons.Default.Folder, Icons.Default.Image, Icons.Default.Videocam, Icons.Default.Article).

A Text element in the middle, showing the full filename. It should be configured to handle long names gracefully with ellipsis.

A three-dots IconButton (Icons.Default.MoreVert) on the right to open a context menu.

Content (Grid View): A Column containing:

A large Icon representing the file type.

A Text element below it for the filename.

DropdownMenu (Context Menu):

Triggered by the three-dots icon or a long press.

Contains DropdownMenuItems for "View", "Unhide", and "Delete".