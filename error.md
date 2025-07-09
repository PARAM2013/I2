Logcat logs.


2025-07-09 12:15:52.498 30690-30695 .example.secure         com.example.secure                   W  Cleared Reference was only reachable from finalizer (only reported once)
2025-07-09 12:15:52.658 30690-30690 .example.secure         com.example.secure                   W  Verification of boolean androidx.savedstate.SavedStateReader.contentDeepEquals-impl(android.os.Bundle, android.os.Bundle) took 154.888ms (71.02 bytecodes/s) (0B arena alloc)
2025-07-09 12:15:52.801 30690-30690 WindowOnBackDispatcher  com.example.secure                   W  sendCancelIfRunning: isInProgress=false callback=android.app.Activity$$ExternalSyntheticLambda0@1800807
2025-07-09 12:15:52.832 30690-30690 ViewRootImpl            com.example.secure                   D  Skipping stats log for color mode
---------------------------- PROCESS ENDED (30690) for package com.example.secure ----------------------------
2025-07-09 12:16:06.533 30806-30806 nativeloader            com.example.secure                   D  Load libframework-connectivity-tiramisu-jni.so using APEX ns com_android_tethering for caller /apex/com.android.tethering/javalib/framework-connectivity-t.jar: ok
2025-07-09 12:16:06.811 30806-30806 re-initialized>         com.example.secure                   W  type=1400 audit(0.0:10739): avc:  granted  { execute } for  path="/data/data/com.example.secure/code_cache/startup_agents/9758b833-agent.so" dev="dm-55" ino=82124 scontext=u:r:untrusted_app:s0:c225,c256,c512,c768 tcontext=u:object_r:app_data_file:s0:c225,c256,c512,c768 tclass=file app=com.example.secure
2025-07-09 12:16:06.841 30806-30806 nativeloader            com.example.secure                   D  Load /data/user/0/com.example.secure/code_cache/startup_agents/9758b833-agent.so using system ns (caller=<unknown>): ok
2025-07-09 12:16:06.876 30806-30806 .example.secure         com.example.secure                   W  hiddenapi: DexFile /data/data/com.example.secure/code_cache/.studio/instruments-07dd17c6.jar is in boot class path but is not in a known location
2025-07-09 12:16:07.106 30806-30806 .example.secure         com.example.secure                   W  Redefining intrinsic method java.lang.Thread java.lang.Thread.currentThread(). This may cause the unexpected use of the original definition of java.lang.Thread java.lang.Thread.currentThread()in methods that have already been compiled.
2025-07-09 12:16:07.106 30806-30806 .example.secure         com.example.secure                   W  Redefining intrinsic method boolean java.lang.Thread.interrupted(). This may cause the unexpected use of the original definition of boolean java.lang.Thread.interrupted()in methods that have already been compiled.
2025-07-09 12:16:07.126 30806-30806 ActivityThread          com.example.secure                   I  Relaunch all activities: onCoreSettingsChange
2025-07-09 12:16:07.140 30806-30806 nativeloader            com.example.secure                   D  Load libstats_jni.so using APEX ns com_android_os_statsd for caller /apex/com.android.os.statsd/javalib/framework-statsd.jar: ok
---------------------------- PROCESS STARTED (30806) for package com.example.secure ----------------------------
2025-07-09 12:16:07.311 30806-30806 ApplicationLoaders      com.example.secure                   D  Returning zygote-cached class loader: /system_ext/framework/androidx.window.extensions.jar
2025-07-09 12:16:07.311 30806-30806 ApplicationLoaders      com.example.secure                   D  Returning zygote-cached class loader: /system_ext/framework/androidx.window.sidecar.jar
2025-07-09 12:16:08.958 30806-30806 nativeloader            com.example.secure                   D  Configuring clns-9 for other apk /data/app/~~bg4uti3_m0tDa23Xrry7-A==/com.example.secure-vXlzs9UxpHyIki71bfcGdA==/base.apk. target_sdk_version=36, uses_libraries=, library_path=/data/app/~~bg4uti3_m0tDa23Xrry7-A==/com.example.secure-vXlzs9UxpHyIki71bfcGdA==/lib/x86_64:/data/app/~~bg4uti3_m0tDa23Xrry7-A==/com.example.secure-vXlzs9UxpHyIki71bfcGdA==/base.apk!/lib/x86_64, permitted_path=/data:/mnt/expand:/data/user/0/com.example.secure
2025-07-09 12:16:08.990 30806-30806 .example.secure         com.example.secure                   I  AssetManager2(0x73a96ac898d8) locale list changing from [] to [en-US]
2025-07-09 12:16:08.999 30806-30806 .example.secure         com.example.secure                   I  AssetManager2(0x73a96ac8d118) locale list changing from [] to [en-US]
2025-07-09 12:16:09.048 30806-30806 GraphicsEnvironment     com.example.secure                   V  Currently set values for:
2025-07-09 12:16:09.048 30806-30806 GraphicsEnvironment     com.example.secure                   V    angle_gl_driver_selection_pkgs=[]
2025-07-09 12:16:09.048 30806-30806 GraphicsEnvironment     com.example.secure                   V    angle_gl_driver_selection_values=[]
2025-07-09 12:16:09.048 30806-30806 GraphicsEnvironment     com.example.secure                   V  com.example.secure is not listed in per-application setting
2025-07-09 12:16:09.049 30806-30806 GraphicsEnvironment     com.example.secure                   V  ANGLE allowlist from config:
2025-07-09 12:16:09.049 30806-30806 GraphicsEnvironment     com.example.secure                   V  com.example.secure is not listed in ANGLE allowlist or settings, returning default
2025-07-09 12:16:09.050 30806-30806 GraphicsEnvironment     com.example.secure                   V  Neither updatable production driver nor prerelease driver is supported.
2025-07-09 12:16:09.213 30806-30820 DisplayManager          com.example.secure                   I  Choreographer implicitly registered for the refresh rate.
2025-07-09 12:16:09.221 30806-30806 .example.secure         com.example.secure                   I  AssetManager2(0x73a96ac8d758) locale list changing from [] to [en-US]
2025-07-09 12:16:09.341 30806-30806 AppCompatDelegate       com.example.secure                   D  Checking for metadata for AppLocalesMetadataHolderService : Service not found
2025-07-09 12:16:09.344 30806-30806 .example.secure         com.example.secure                   I  AssetManager2(0x73a96ac93838) locale list changing from [] to [en-US]
2025-07-09 12:16:09.367 30806-30820 EGL_emulation           com.example.secure                   I  Opening libGLESv1_CM_emulation.so
2025-07-09 12:16:09.370 30806-30820 EGL_emulation           com.example.secure                   I  Opening libGLESv2_emulation.so
2025-07-09 12:16:09.374 30806-30806 ashmem                  com.example.secure                   E  Pinning is deprecated since Android Q. Please use trim or other methods.
2025-07-09 12:16:09.493 30806-30820 HWUI                    com.example.secure                   W  Failed to choose config with EGL_SWAP_BEHAVIOR_PRESERVED, retrying without...
2025-07-09 12:16:09.493 30806-30820 HWUI                    com.example.secure                   W  Failed to initialize 101010-2 format, error = EGL_SUCCESS
2025-07-09 12:16:09.642 30806-30806 .example.secure         com.example.secure                   E  Invalid resource ID 0x00000000.
2025-07-09 12:16:09.668 30806-30806 CompatChangeReporter    com.example.secure                   D  Compat change id reported: 377864165; UID 10225; state: ENABLED
2025-07-09 12:16:09.672 30806-30806 DesktopModeFlags        com.example.secure                   D  Toggle override initialized to: OVERRIDE_UNSET
2025-07-09 12:16:09.737 30806-30806 HWUI                    com.example.secure                   W  Image decoding logging dropped!
2025-07-09 12:16:09.746 30806-30806 HWUI                    com.example.secure                   W  Image decoding logging dropped!
2025-07-09 12:16:09.766 30806-30806 HWUI                    com.example.secure                   W  Image decoding logging dropped!
2025-07-09 12:16:10.098 30806-30806 .example.secure         com.example.secure                   I  hiddenapi: Accessing hidden method Landroid/view/ViewGroup;->makeOptionalFitsSystemWindows()V (runtime_flags=0, domain=platform, api=unsupported) from Landroidx/appcompat/widget/ViewUtils; (domain=app) using reflection: allowed
2025-07-09 12:16:10.315 30806-30806 HWUI                    com.example.secure                   W  Unknown dataspace 0
2025-07-09 12:16:10.938 30806-30806 AssistStructure         com.example.secure                   I  Flattened final assist data: 4012 bytes, containing 1 windows, 24 views
2025-07-09 12:16:11.378 30806-30810 .example.secure         com.example.secure                   I  Compiler allocated 5042KB to compile void android.view.ViewRootImpl.performTraversals()
2025-07-09 12:16:12.302 30806-30806 ImeTracker              com.example.secure                   I  com.example.secure:cc247f2a: onRequestShow at ORIGIN_CLIENT reason SHOW_SOFT_INPUT fromUser false
2025-07-09 12:16:12.309 30806-30806 InsetsController        com.example.secure                   D  show(ime(), fromIme=false)
2025-07-09 12:16:12.313 30806-30806 InsetsController        com.example.secure                   D  Setting requestedVisibleTypes to -1 (was -9)
2025-07-09 12:16:13.050 30806-30835 InteractionJankMonitor  com.example.secure                   W  Initializing without READ_DEVICE_CONFIG permission. enabled=false, interval=1, missedFrameThreshold=3, frameTimeThreshold=64, package=com.example.secure
2025-07-09 12:16:13.251 30806-30806 ImeTracker              com.example.secure                   I  com.example.secure:cc247f2a: onShown
2025-07-09 12:16:15.041 30806-30840 ProfileInstaller        com.example.secure                   D  Installing profile for com.example.secure
2025-07-09 12:16:15.488 30806-30806 HWUI                    com.example.secure                   W  Image decoding logging dropped!
2025-07-09 12:16:15.537 30806-30806 MainActivity            com.example.secure                   D  Permissions granted or handled. Attempting to load vault content.
2025-07-09 12:16:16.342 30806-30811 .example.secure         com.example.secure                   I  Background concurrent mark compact GC freed 4714KB AllocSpace bytes, 6(120KB) LOS objects, 49% free, 4659KB/9319KB, paused 1.472ms,7.710ms total 68.777ms
2025-07-09 12:16:16.410 30806-30806 MainDashboardViewModel  com.example.secure                   D  Loading vault files...
2025-07-09 12:16:16.448 30806-30806 FileManager             com.example.secure                   D  Vault directory: /storage/emulated/0/.iSecureVault
2025-07-09 12:16:16.552 30806-30806 MainDashboardViewModel  com.example.secure                   D  Loaded 3 items (files and folders).
2025-07-09 12:16:17.536 30806-30815 HWUI                    com.example.secure                   I  Davey! duration=1920ms; Flags=1, FrameTimelineVsyncId=2408599, IntendedVsync=49176312909373, Vsync=49176312909373, InputEventId=0, HandleInputStart=49176319761237, AnimationStart=49176319790437, PerformTraversalsStart=49176319835637, DrawStart=49178048938937, FrameDeadline=49176329576039, FrameStartTime=49176319741137, FrameInterval=16666666, WorkloadTarget=16666666, SyncQueued=49178094426537, SyncStart=49178124689337, IssueDrawCommandsStart=49178124901237, SwapBuffers=49178257758437, FrameCompleted=49178264023237, DequeueBufferDuration=51300, QueueBufferDuration=779800, GpuCompleted=49178264023237, SwapBuffersCompleted=49178259376637, DisplayPresentTime=-70582689400013720, CommandSubmissionCompleted=49178257758437,
2025-07-09 12:16:17.574 30806-30806 Choreographer           com.example.secure                   I  Skipped 102 frames!  The application may be doing too much work on its main thread.
2025-07-09 12:16:17.577 30806-30806 VRI[LockScreenActivity] com.example.secure                   D  visibilityChanged oldVisibility=true newVisibility=false
2025-07-09 12:16:17.580 30806-30806 ImeTracker              com.example.secure                   I  system_server:e39f4a23: onCancelled at PHASE_CLIENT_ON_CONTROLS_CHANGED
2025-07-09 12:16:17.597 30806-30806 WindowOnBackDispatcher  com.example.secure                   W  sendCancelIfRunning: isInProgress=false callback=android.view.ImeBackAnimationController@c9e859d
2025-07-09 12:16:17.609 30806-30815 HWUI                    com.example.secure                   I  Davey! duration=1733ms; Flags=0, FrameTimelineVsyncId=2408756, IntendedVsync=49176596242695, Vsync=49178296242627, InputEventId=0, HandleInputStart=49178309144737, AnimationStart=49178309174837, PerformTraversalsStart=49178309216437, DrawStart=49178309920137, FrameDeadline=49178296242627, FrameStartTime=49178307404137, FrameInterval=16666666, WorkloadTarget=16666666, SyncQueued=49178310042237, SyncStart=49178310388437, IssueDrawCommandsStart=49178310516437, SwapBuffers=49178311583337, FrameCompleted=49178330292937, DequeueBufferDuration=64200, QueueBufferDuration=2193200, GpuCompleted=49178330292937, SwapBuffersCompleted=49178317107737, DisplayPresentTime=-69624284627558846, CommandSubmissionCompleted=49178311583337,
2025-07-09 12:16:17.646 30806-30806 InsetsController        com.example.secure                   D  hide(ime(), fromIme=false)
2025-07-09 12:16:17.646 30806-30806 ImeTracker              com.example.secure                   I  com.example.secure:4f955f14: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
2025-07-09 12:16:17.665 30806-30806 ImeTracker              com.example.secure                   I  system_server:ab244bf1: onCancelled at PHASE_CLIENT_ON_CONTROLS_CHANGED
2025-07-09 12:16:17.678 30806-30806 AutofillManager         com.example.secure                   I  onInvisibleForAutofill(): expiringResponse
2025-07-09 12:16:17.686 30806-30806 WindowOnBackDispatcher  com.example.secure                   W  sendCancelIfRunning: isInProgress=false callback=android.app.Activity$$ExternalSyntheticLambda0@def0d4c
2025-07-09 12:16:17.701 30806-30806 ViewRootImpl            com.example.secure                   D  Skipping stats log for color mode
2025-07-09 12:16:17.710 30806-30806 RemoteInpu...ectionImpl com.example.secure                   W  requestCursorUpdates on inactive InputConnection
2025-07-09 12:16:17.729 30806-30806 WindowOnBackDispatcher  com.example.secure                   W  sendCancelIfRunning: isInProgress=false callback=android.view.ImeBackAnimationController@6c624d7
2025-07-09 12:16:17.872 30806-30806 WindowOnBackDispatcher  com.example.secure                   W  sendCancelIfRunning: isInProgress=false callback=android.view.ImeBackAnimationController@6c624d7
