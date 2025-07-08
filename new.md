# 📱 Project Blueprint: iSecure

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

---

## 💾 2. Persistent Secure Storage (Post Uninstall)

* Files are stored in internal device storage, in a non-app-private folder.
* If the app is uninstalled:

  * Files remain intact.
  * Upon reinstall, the app reconnects and reads old secure data.

---

## 🗂️ 3. Secure Dashboard UI

User sees 4 main categories after login:

* **All Files** (e.g. 4 Folders, 225 MB)

* **Photos** (e.g. 1 File, 2.5 MB)

* **Videos** (e.g. 0 Files, 0 MB)

* **Documents** (e.g. 1 File, 3.1 MB)

* Floating Action Button (FAB):

  * Create Folder
  * Import File

---

## 📝 4. File & Folder Management

* Files added to iSecure are **moved** from original location.
* Original files are deleted after import.
* File metadata (EXIF, location info) is removed if setting is enabled.

---

## 🔄 5. File Sharing Rules

* **Allowed:**

  * PDF and other document formats

* **Blocked:**

  * Photos (JPG, PNG, etc.)
  * Videos (MP4, etc.)

* If blocked file is selected for sharing, show message:

  * “Only PDF or document sharing is allowed.”

---

## ⚙️ 6. Settings Screen Features

* Change App PIN
* Enable / Disable Fingerprint Unlock
* Enable / Disable Metadata Removal (on import)

---

## 📥 7. Unhide / Restore Files

* Unhidden files are moved to:

  * `Downloads/SecureUnhide/

* Show confirmation:

  * ✅ “File restored to Downloads/SecureUnhide/”

---

## 🔒 8. Optional Features (Future Scope)

* Fake PIN (decoy mode)
* Intruder capture (photo on failed login)


---

## 🔢 9. System Default Viewer Integration

* Users can open/view files using Android's default system viewer apps (e.g., PDF Viewer, Gallery, Video Player).
* App will use `Intent.ACTION_VIEW` with appropriate MIME type and `FileProvider` URI.

### Permissions Required:

* `android.permission.READ_EXTERNAL_STORAGE`
* `android.permission.WRITE_EXTERNAL_STORAGE` *(for SDK < 29)*
* For Android 11+ (SDK 30+), use `MANAGE_EXTERNAL_STORAGE` only if absolutely necessary (subject to Play Store policies).

### File Viewing Method:

```kotlin
val intent = Intent(Intent.ACTION_VIEW)
intent.setDataAndType(fileUri, mimeType)
intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
startActivity(Intent.createChooser(intent, "Open with"))
```

> Ensure `FileProvider` is declared in `AndroidManifest.xml` and correctly configured in `res/xml/file_paths.xml`.
