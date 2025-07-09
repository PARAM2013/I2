# Project Overview: iSecure - Secure File Vault

This document provides a comprehensive overview of the "iSecure" Android application, a secure file vault designed to protect user data.

## Key Features

*   **Authentication:**
    *   **PIN-based Access:** Users set a 4-digit PIN to secure the application. The app enforces PIN verification for access.
    *   **Biometric Integration:** Supports fingerprint authentication as a convenient alternative to the PIN. This can be enabled or disabled in the settings.

*   **Secure File Management:**
    *   **Hidden Vault Storage:** Files are stored in a hidden directory (`.iSecureVault`) in the public external storage. This ensures that the files persist even if the application is uninstalled.
    *   **Import/Export Functionality:** Users can import files from their device into the secure vault and export (unhide) them back to a public directory (`SecureUnhide` within the Downloads folder).
    *   **Folder Organization:** The vault supports the creation of subfolders for better organization of stored files.
    *   **File & Folder Listing:** The application provides a detailed listing of files and folders within the vault, including categorization (photos, videos, documents, others) and file size information.
    *   **Data Deletion:** Allows for the secure deletion of files and folders from within the vault.

*   **User Experience & Interface:**
    *   **Activity Flow:** The application manages user flow through `LauncherActivity` (initial setup/lock check), `MainActivity` (main content), `PinSetupActivity`, and `LockScreenActivity`.
    *   **Modern UI (Jetpack Compose):** The main dashboard is built using Jetpack Compose, providing a modern and responsive user interface.
    *   **Traditional Views:** The PIN input and lock screens are built with traditional Android Views.

*   **Security & Permissions:**
    *   **Runtime Permissions:** The application handles necessary Android runtime permissions, including `MANAGE_EXTERNAL_STORAGE` for Android 11 (API 30) and above, to ensure proper file access.
    *   **Inactivity Lock:** An automatic inactivity timer (90 seconds) is implemented to lock the application if it remains idle in the foreground, enhancing security.
    *   **PIN Security:** PINs are now securely hashed using PBKDF2WithHmacSHA1 and a unique salt for each PIN, enhancing security.

## Technical Architecture

The application follows a structured approach, leveraging standard Android components and modern development practices.

*   **Component-Based Design:** Utilizes a combination of Android Activities and Fragments to modularize different screens and functionalities.
*   **Data Management:** The `FileManager` object serves as a central point for all file-related operations, acting as a data layer for interacting with the secure vault.
*   **Global State Management:** `AppGlobalState` is a singleton object that maintains the application's global state, such as its locked status and the currently active `Activity`. This is crucial for the auto-lock feature.
*   **UI Frameworks:** Employs both traditional Android Views (for PIN input keypads) and Jetpack Compose for the main dashboard.

### Code Breakdown

*   **`MainActivity.kt`**: The main entry point after authentication. It sets up the Jetpack Compose UI and handles permission requests.
*   **`FileManager.kt`**: A singleton object that manages all file operations.
    *   `getVaultDirectory()`: Returns the hidden vault directory.
    *   `getUnhideDirectory()`: Returns the directory for exported files.
    *   `checkStoragePermissions()` & `requestStoragePermissions()`: Handle storage permissions for different Android versions.
    *   `deleteFileFromVault()`: Deletes a file or folder from the vault.
    *   `unhideFile()`: Moves a file from the vault to the public "unhide" directory.
    *   `listFilesInVault()`: Returns a `VaultStats` object with a detailed breakdown of the vault's contents.
    *   `createSubFolderInVault()`: Creates a new folder within the vault.
    *   `importFile()`: Imports a file into the vault.
*   **`PinManager.kt`**: A singleton object for managing the user's PIN and fingerprint authentication settings.
    *   `savePin()`, `getPin()`, `isPinSet()`, `verifyPin()`, `clearPin()`: Basic PIN management functions.
    *   `setFingerprintAuthEnabled()`, `isFingerprintAuthEnabled()`: Manage fingerprint authentication preferences.
*   **`AppGlobalState.kt`**: A singleton for managing the application's global state.
    *   `isLocked`: A boolean that tracks if the app is locked.
    *   `currentActivity`: Tracks the current foreground activity.
    *   `onActivityResumed()`, `onActivityPaused()`, `onUserInteraction()`: Manage the inactivity timer for the auto-lock feature.
*   **`LauncherActivity.kt`**: The initial activity that checks if a PIN is set and navigates to either `PinSetupActivity` or `LockScreenActivity`.
*   **`PinSetupActivity.kt`**: An activity for setting up the user's PIN for the first time.
*   **`LockScreenActivity.kt`**: The activity that prompts the user for their PIN or biometric authentication to unlock the app.
*   **`SecureDashboardFragment.kt`**: A fragment that displays the vault's contents. It uses a `ViewModel` (`SecureDashboardViewModel`) to interact with the `FileManager` and update the UI.
*   **`MainDashboardScreen.kt`**: A Jetpack Compose screen that displays the main dashboard UI.


## Run build command after code update
## Update this file after major changes in project , code etc  
