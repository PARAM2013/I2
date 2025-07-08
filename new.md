# 📱 Project Blueprint: iSecure

**Overall Progress:** Core file management logic and dashboard integration for statistics and basic operations are implemented. Settings screen, advanced file operations (sharing, metadata), and file listing/viewing are pending.

---

## 🔐 1. App Access & Security

* **First Launch**

  * User must set a 4-digit App PIN.
  * After PIN setup, user is directed to the secure dashboard.

* **Unlock Options** (Configurable in Settings):

  * App PIN (mandatory)
  * Fingerprint authentication (optional)
  
* **Auto Lock:**

  * App locks automatically after inactivity or background (default 90 seconds).
  * Re-opening the app triggers lock screen.
**Status: COMPLETED (as per initial problem description)**
---

## 💾 2. Persistent Secure Storage (Post Uninstall)

* Files are stored in internal device storage, in a non-app-private folder.
  * **DONE** (Vault: `.iSecureVault` in public external storage, Unhide: `Downloads/SecureUnhide/`)
  * Methods used: `FileManager.getVaultDirectory()`, `FileManager.getUnhideDirectory()`
* If the app is uninstalled:
  * Files remain intact.
    * **DONE** (by using public external storage)
  * Upon reinstall, the app reconnects and reads old secure data.
    * **PARTIALLY DONE** (App can read from the vault if permissions are granted. Explicit reconnection logic post-reinstall not specifically added but directory structure supports it.)
**Status: LARGELY DONE** (Core mechanism in place)
---

## 🗂️ 3. Secure Dashboard UI

User sees 4 main categories after login:

* **All Files** (e.g. 4 Folders, 225 MB)

* **Photos** (e.g. 1 File, 2.5 MB)

* **Videos** (e.g. 0 Files, 0 MB)

* **Documents** (e.g. 1 File, 3.1 MB)

* Floating Action Button (FAB):
  * Create Folder
    * **DONE** (`SecureDashboardFragment.showCreateFolderDialog()`, `FileManager.createSubFolderInVault()`)
  * Import File
    * **DONE** (`SecureDashboardFragment.importFileLauncher` using `ActivityResultContracts.GetContent()`, `FileManager.importFile()`)
**Status: DONE** (UI elements connected to `FileManager` for statistics and basic import/create folder actions)
---

## 📝 4. File & Folder Management

* Files added to iSecure are **moved** from original location.
* Original files are deleted after import.
  * **PARTIALLY DONE**. `FileManager.importFile()` attempts to delete original. Works for `file://` URIs. For `content://` URIs (SAF), it now attempts `DocumentsContract.deleteDocument`, but success depends on URI permissions. This is an improvement but not guaranteed for all cases.
* File metadata (EXIF, location info) is removed if setting is enabled.
  * **PENDING** (No logic implemented yet for metadata removal)

**Other Implemented File Management in `FileManager.kt`:**
* Listing files/folders and calculating statistics: `FileManager.listFilesInVault()`, `VaultStats`, `VaultFolder`, `VaultFile`, `FileCategory`, `getFileCategory()`
* Deleting files/folders from vault: `FileManager.deleteFileFromVault()` (UI for this is pending file listing)

**Status: PARTIALLY DONE** (Core import, creation, deletion logic in place. Metadata removal pending. Full "move" for import is best-effort.)
---

## 🔄 5. File Sharing Rules

* **Allowed:**

  * PDF and other document formats

* **Blocked:**

  * Photos (JPG, PNG, etc.)
  * Videos (MP4, etc.)

* If blocked file is selected for sharing, show message:

  * “Only PDF or document sharing is allowed.”
**Status: PENDING**
---

## ⚙️ 6. Settings Screen Features

* Change App PIN
  * **PENDING**
* Enable / Disable Fingerprint Unlock
  * **PENDING**
* Enable / Disable Metadata Removal (on import)
  * **PENDING**
**Status: PENDING**
---

## 📥 7. Unhide / Restore Files

* Unhidden files are moved to:
  * `Downloads/SecureUnhide/`
    * **DONE** (`FileManager.getUnhideDirectory()`, `FileManager.unhideFile()`)
* Show confirmation:
  * ✅ “File restored to Downloads/SecureUnhide/”
    * **PENDING** (Actual confirmation UI toast/dialog after unhide operation is pending UI for triggering unhide)
**Status: PARTIALLY DONE** (Core `unhideFile` logic implemented in `FileManager`. UI for triggering and confirmation is pending file listing.)
---

## 🔒 8. Optional Features (Future Scope)

* Fake PIN (decoy mode)
  * **PENDING**
* Intruder capture (photo on failed login)
  * **PENDING**
**Status: PENDING**
---

## 🔢 9. System Default Viewer Integration

* Users can open/view files using Android's default system viewer apps (e.g., PDF Viewer, Gallery, Video Player).
  * **PENDING**
* App will use `Intent.ACTION_VIEW` with appropriate MIME type and `FileProvider` URI.
  * **PENDING**

### Permissions Required:

* `android.permission.READ_EXTERNAL_STORAGE`
* `android.permission.WRITE_EXTERNAL_STORAGE` *(for SDK < 29)*
* For Android 11+ (SDK 30+), use `MANAGE_EXTERNAL_STORAGE` only if absolutely necessary (subject to Play Store policies).
  * **DONE** (Implemented in `FileManager.checkStoragePermissions()`, `FileManager.requestStoragePermissions()`, and `MainActivity` handles the flow for `MANAGE_EXTERNAL_STORAGE` or legacy permissions).
**Status: PARTIALLY DONE** (Permission handling for storage is done. File viewing logic is pending.)

### File Viewing Method:

```kotlin
val intent = Intent(Intent.ACTION_VIEW)
intent.setDataAndType(fileUri, mimeType)
intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
startActivity(Intent.createChooser(intent, "Open with"))
```

> Ensure `FileProvider` is declared in `AndroidManifest.xml` and correctly configured in `res/xml/file_paths.xml`.
