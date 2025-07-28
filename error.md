logcat


2025-07-28 13:32:01.062 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:01.067 18075-18075 FileManager             com.example.secure                   D  createSubFolderInVault: Folder created successfully: /storage/emulated/0/.iSecureVault/123
2025-07-28 13:32:01.071 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:01.644 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:01.652 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:02.787 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:02.789 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:02.791 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:02.796 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:08.770 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:08.772 18075-18075 FileManager             com.example.secure                   D  importFile: Importing file from content://com.android.providers.media.documents/document/image%3A1000166682 to /storage/emulated/0/.iSecureVault/123/IMG_20250728_132931.jpg
2025-07-28 13:32:08.772 18075-18075 FileManager             com.example.secure                   D  Metadata removal enabled for IMG_20250728_132931.jpg
2025-07-28 13:32:08.790 18075-18075 FileManager             com.example.secure                   D  Copied to temp file for metadata stripping: /data/user/0/com.example.secure/cache/metadata_strip_8021982757778636848.jpg
2025-07-28 13:32:08.818 18075-18075 FileManager             com.example.secure                   D  Metadata stripped from temp file.
2025-07-28 13:32:08.844 18075-18075 FileManager             com.example.secure                   D  importFile: File copied successfully to /storage/emulated/0/.iSecureVault/123/IMG_20250728_132931.jpg
2025-07-28 13:32:08.848 18075-18075 DocumentsContract       com.example.secure                   W  Failed to delete document (Ask Gemini)
java.lang.SecurityException: Permission Denial: writing com.android.providers.media.MediaDocumentsProvider uri content://com.android.providers.media.documents/document/image%3A1000166682 from pid=18075, uid=10356 requires android.permission.MANAGE_DOCUMENTS, or grantUriPermission()
at android.os.Parcel.createExceptionOrNull(Parcel.java:3011)
at android.os.Parcel.createException(Parcel.java:2995)
at android.os.Parcel.readException(Parcel.java:2978)
at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:190)
at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:142)
at android.content.ContentProviderProxy.call(ContentProviderNative.java:752)
at android.content.ContentResolver.call(ContentResolver.java:2450)
at android.provider.DocumentsContract.deleteDocument(DocumentsContract.java:1469)
at com.example.secure.file.FileManager.importFile(FileManager.kt:574)
at com.example.secure.ui.dashboard.MainDashboardViewModel$importFiles$3.invokeSuspend(MainDashboardViewModel.kt:293)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.internal.DispatchedContinuationKt.resumeCancellableWith(DispatchedContinuation.kt:367)
at kotlinx.coroutines.intrinsics.CancellableKt.startCoroutineCancellable(Cancellable.kt:30)
at kotlinx.coroutines.intrinsics.CancellableKt.startCoroutineCancellable$default(Cancellable.kt:25)
at kotlinx.coroutines.CoroutineStart.invoke(CoroutineStart.kt:110)
at kotlinx.coroutines.AbstractCoroutine.start(AbstractCoroutine.kt:126)
at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch(Builders.common.kt:56)
at kotlinx.coroutines.BuildersKt.launch(Unknown Source:1)
at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch$default(Builders.common.kt:47)
at kotlinx.coroutines.BuildersKt.launch$default(Unknown Source:1)
at com.example.secure.ui.dashboard.MainDashboardViewModel.importFiles(MainDashboardViewModel.kt:286)
at com.example.secure.ui.allfiles.AllFilesScreenKt$AllFilesScreen$filePickerLauncher$1.invoke(AllFilesScreen.kt:111)
at com.example.secure.ui.allfiles.AllFilesScreenKt$AllFilesScreen$filePickerLauncher$1.invoke(AllFilesScreen.kt:107)
at androidx.activity.compose.ActivityResultRegistryKt$rememberLauncherForActivityResult$1$1.invoke$lambda$0(ActivityResultRegistry.kt:106)
at androidx.activity.compose.ActivityResultRegistryKt$rememberLauncherForActivityResult$1$1.$r8$lambda$VLs2Oqd6MzDD-LGilzyhNhDSH_4(Unknown Source:0)
at androidx.activity.compose.ActivityResultRegistryKt$rememberLauncherForActivityResult$1$1$$ExternalSyntheticLambda0.onActivityResult(D8$$SyntheticClass:0)
at androidx.activity.result.ActivityResultRegistry.doDispatch(ActivityResultRegistry.kt:371)
at androidx.activity.result.ActivityResultRegistry.dispatchResult(ActivityResultRegistry.kt:331)
at androidx.activity.ComponentActivity.onActivityResult(ComponentActivity.kt:786)
at androidx.fragment.app.FragmentActivity.onActivityResult(FragmentActivity.java:151)
at com.example.secure.MainActivity.onActivityResult(MainActivity.kt:193)
at android.app.Activity.dispatchActivityResult(Activity.java:8962)
at android.app.ActivityThread.deliverResults(ActivityThread.java:5608)
at android.app.ActivityThread.handleSendResult(ActivityThread.java:5659)
at android.app.servertransaction.ActivityResultItem.execute(ActivityResultItem.java:67)
at android.app.servertransaction.ActivityTransactionItem.execute(ActivityTransactionItem.java:45)
at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135)
at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95)
at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2443)
at android.os.Handler.dispatchMessage(Handler.java:106)
at android.os.Looper.loopOnce(Looper.java:211)
at android.os.Looper.loop(Looper.java:300)
at android.app.ActivityThread.main(ActivityThread.java:8348)
2025-07-28 13:32:08.849 18075-18075 FileManager             com.example.secure                   E  importFile: SecurityException when trying to delete original document: content://com.android.providers.media.documents/document/image%3A1000166682 (Ask Gemini)
java.lang.SecurityException: Permission Denial: writing com.android.providers.media.MediaDocumentsProvider uri content://com.android.providers.media.documents/document/image%3A1000166682 from pid=18075, uid=10356 requires android.permission.MANAGE_DOCUMENTS, or grantUriPermission()
at android.os.Parcel.createExceptionOrNull(Parcel.java:3011)
at android.os.Parcel.createException(Parcel.java:2995)
at android.os.Parcel.readException(Parcel.java:2978)
at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:190)
at android.database.DatabaseUtils.readExceptionFromParcel(DatabaseUtils.java:142)
at android.content.ContentProviderProxy.call(ContentProviderNative.java:752)
at android.content.ContentResolver.call(ContentResolver.java:2450)
at android.provider.DocumentsContract.deleteDocument(DocumentsContract.java:1469)
at com.example.secure.file.FileManager.importFile(FileManager.kt:574)
at com.example.secure.ui.dashboard.MainDashboardViewModel$importFiles$3.invokeSuspend(MainDashboardViewModel.kt:293)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.internal.DispatchedContinuationKt.resumeCancellableWith(DispatchedContinuation.kt:367)
at kotlinx.coroutines.intrinsics.CancellableKt.startCoroutineCancellable(Cancellable.kt:30)
at kotlinx.coroutines.intrinsics.CancellableKt.startCoroutineCancellable$default(Cancellable.kt:25)
at kotlinx.coroutines.CoroutineStart.invoke(CoroutineStart.kt:110)
at kotlinx.coroutines.AbstractCoroutine.start(AbstractCoroutine.kt:126)
at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch(Builders.common.kt:56)
at kotlinx.coroutines.BuildersKt.launch(Unknown Source:1)
at kotlinx.coroutines.BuildersKt__Builders_commonKt.launch$default(Builders.common.kt:47)
at kotlinx.coroutines.BuildersKt.launch$default(Unknown Source:1)
at com.example.secure.ui.dashboard.MainDashboardViewModel.importFiles(MainDashboardViewModel.kt:286)
at com.example.secure.ui.allfiles.AllFilesScreenKt$AllFilesScreen$filePickerLauncher$1.invoke(AllFilesScreen.kt:111)
at com.example.secure.ui.allfiles.AllFilesScreenKt$AllFilesScreen$filePickerLauncher$1.invoke(AllFilesScreen.kt:107)
at androidx.activity.compose.ActivityResultRegistryKt$rememberLauncherForActivityResult$1$1.invoke$lambda$0(ActivityResultRegistry.kt:106)
at androidx.activity.compose.ActivityResultRegistryKt$rememberLauncherForActivityResult$1$1.$r8$lambda$VLs2Oqd6MzDD-LGilzyhNhDSH_4(Unknown Source:0)
at androidx.activity.compose.ActivityResultRegistryKt$rememberLauncherForActivityResult$1$1$$ExternalSyntheticLambda0.onActivityResult(D8$$SyntheticClass:0)
at androidx.activity.result.ActivityResultRegistry.doDispatch(ActivityResultRegistry.kt:371)
at androidx.activity.result.ActivityResultRegistry.dispatchResult(ActivityResultRegistry.kt:331)
at androidx.activity.ComponentActivity.onActivityResult(ComponentActivity.kt:786)
at androidx.fragment.app.FragmentActivity.onActivityResult(FragmentActivity.java:151)
at com.example.secure.MainActivity.onActivityResult(MainActivity.kt:193)
at android.app.Activity.dispatchActivityResult(Activity.java:8962)
at android.app.ActivityThread.deliverResults(ActivityThread.java:5608)
at android.app.ActivityThread.handleSendResult(ActivityThread.java:5659)
at android.app.servertransaction.ActivityResultItem.execute(ActivityResultItem.java:67)
at android.app.servertransaction.ActivityTransactionItem.execute(ActivityTransactionItem.java:45)
at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135)
at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95)
at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2443)
at android.os.Handler.dispatchMessage(Handler.java:106)
at android.os.Looper.loopOnce(Looper.java:211)
2025-07-28 13:32:08.849 18075-18075 FileManager             com.example.secure                   E  	at android.os.Looper.loop(Looper.java:300) (Ask Gemini)
at android.app.ActivityThread.main(ActivityThread.java:8348)
at java.lang.reflect.Method.invoke(Native Method)
at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:582)
at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1028)
2025-07-28 13:32:08.850 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:08.853 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-28 13:32:08.859 18075-18075 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault


2025-07-28 13:32:01.642 18075-18075 MainDashboardVM         com.example.secure                   D  Path data loaded. Folders: 2, Files: 7 for null
2025-07-28 13:32:02.789 18075-18075 MainDashboardVM         com.example.secure                   D  Loading contents for path: /storage/emulated/0/.iSecureVault/123
2025-07-28 13:32:02.790 18075-18075 MainDashboardVM         com.example.secure                   D  Path data loaded. Folders: 0, Files: 0 for 123
2025-07-28 13:32:08.764 18075-18075 MainDashboardVM         com.example.secure                   D  Importing file: content://com.android.providers.media.documents/document/image%3A1000166682 to path: 123
2025-07-28 13:32:08.851 18075-18075 MainDashboardVM         com.example.secure                   D  Loading contents for path: /storage/emulated/0/.iSecureVault/123
2025-07-28 13:32:08.852 18075-18075 MainDashboardVM         com.example.secure                   D  Path data loaded. Folders: 0, Files: 1 for 123
