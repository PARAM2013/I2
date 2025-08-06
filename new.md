List of video player Controllers & UI Components
The UI can be broken down into several logical groups:

1. Core Playback Controls (Center Overlay)
   These are the most prominent controls that appear in the middle of the screen when the UI is visible.

Play/Pause Button: A large, circular button in the center. It toggles between play and pause states, with the icon changing accordingly.

Action: player.play() / player.pause()

Rewind Button: Typically positioned to the left of the Play/Pause button. It seeks backward by a fixed duration (usually 10 seconds).

Action: player.seekBack() or player.seekTo(player.getCurrentPosition() - 10000)

Forward Button: Positioned to the right of the Play/Pause button. It seeks forward by a fixed duration (usually 10 seconds).

Action: player.seekForward() or player.seekTo(player.getCurrentPosition() + 10000)

2. Progress and Timing (Bottom Bar)
   This horizontal bar at the bottom provides context about the video's timeline.

Current Time Position: Displays the elapsed time of the video, usually in an MM:SS or HH:MM:SS format.

Data: player.getCurrentPosition()

Seek Bar / Progress Bar: A visual representation of the video's progress. It's interactive, allowing the user to drag ("scrub") to a specific point in the video.

Action: player.seekTo(newPosition)

Total Duration: Displays the total length of the video.

Data: player.getDuration()

Fullscreen Toggle: An icon at the far right of the bottom bar to enter or exit fullscreen mode. This often also handles locking the screen orientation.

Action: Toggles system UI visibility and changes the activity's requested orientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE / PORTRAIT).


3. More Options Menu (Three Dots): A menu on the below-right for less-frequently used settings.

Playback Speed: Allows users to select speeds like 0.5x, 1x (Normal), 1.5x, 2x.

Loop Mode: An option to toggle repeating the video.


4. Gestural Controls (The "Invisible" UI)
   These are crucial for a modern video player experience and are a key part of the Google Files player.

Single Tap (Anywhere on screen): Toggles the visibility of all UI controls (Top bar, bottom bar, and center buttons). The controls should automatically hide after a few seconds of inactivity.

Double Tap (Left Side of Screen): Seeks backward by 10 seconds. An animation or indicator often appears briefly to confirm the action.

Double Tap (Right Side of Screen): Seeks forward by 10 seconds.

Vertical Swipe (Left Side of Screen): Controls screen brightness. Swiping up increases brightness, and swiping down decreases it.

Vertical Swipe (Right Side of Screen): Controls device volume. Swiping up increases volume, and swiping down decreases it.