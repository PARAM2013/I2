# Project Overview: iSecure - Secure File Vault

This document provides an overview of the "iSecure" Android application, a secure file vault designed to protect user data.

## Key Features

*   **Authentication:**
    *   **PIN-based Access:** Users can set and verify a 4-digit PIN to secure the application.
    *   **Biometric Integration:** Supports fingerprint authentication as a convenient alternative to the PIN, with options to enable or disable this feature in settings.
*   **Secure File Management:**
    *   **Hidden Vault Storage:** Files are stored in a dedicated, hidden directory (`.iSecureVault`) within the public external storage, ensuring data persistence even if the application is uninstalled.
    *   **Import/Export Functionality:** Users can import files from their device into the secure vault and "unhide" (export) them back to a designated public directory (`SecureUnhide` within Downloads).
    *   **Folder Organization:** The vault supports the creation of subfolders for better organization of stored files.
    *   **Comprehensive File Listing:** Provides detailed listings of files and folders within the vault, including categorization (photos, videos, documents, others) and file size information.
    *   **Data Deletion:** Allows for secure deletion of files and folders from within the vault.
*   **User Experience & Interface:**
    *   **Activity Flow:** The application manages user flow through `LauncherActivity` (initial setup/lock check), `MainActivity` (main content), `PinSetupActivity`, and `LockScreenActivity`.
    *   **Modern UI (Jetpack Compose):** Key sections like the `FileManagerScreen` and `SecureDashboardFragment` are built using Jetpack Compose, offering a modern and responsive user interface.
    *   **Settings:** A dedicated `SettingsFragment` enables users to configure security preferences, such as fingerprint authentication.
*   **Security & Permissions:**
    *   **Runtime Permissions:** Handles necessary Android runtime permissions, including the `MANAGE_EXTERNAL_STORAGE` permission for Android 11 (API 30) and above, to ensure proper file access.
    *   **Inactivity Lock:** An automatic inactivity timer (90 seconds) is implemented to lock the application if it remains idle in the foreground, enhancing security.
    *   **PIN Security (Future Improvement):** The current PIN storage is noted as needing improvement (hashing) for production-grade security.

## Technical Architecture

The application follows a structured approach, leveraging standard Android components and modern development practices:

*   **Component-Based Design:** Utilizes a combination of Android Activities and Fragments to modularize different screens and functionalities.
*   **Data Management:** The `FileManager` object serves as a central point for all file-related operations, acting as a data layer for interacting with the secure vault.
*   **Global State Management:** `AppGlobalState` is a singleton object that maintains the application's global state, such as its locked status and the currently active `Activity`, crucial for the auto-lock feature.
*   **UI Frameworks:** Employs both traditional Android Views (for PIN input keypads) and Jetpack Compose for building flexible and declarative UI components.

## Potential Areas for Improvement

*   **PIN Hashing:** Implement robust hashing for stored PINs to enhance security.
*   **File Encryption:** Consider adding encryption for files stored within the vault for an additional layer of security.
*   **Error Handling:** Enhance error handling and user feedback for file operations and permission issues.
*   **UI/UX Refinements:** Further refine the user interface and experience, especially for file preview/opening and long-press actions.
*   **Unit/Integration Tests:** Implement comprehensive unit and integration tests to ensure the reliability and correctness of core functionalities.

Run build command after code update