# iSecure Vault - ProGuard Rules for Release Build

# Keep source file names and line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep ExoPlayer classes - Critical for video playback
-keep class com.google.android.exoplayer2.** { *; }
-keep class androidx.media3.** { *; }
-dontwarn com.google.android.exoplayer2.**
-dontwarn androidx.media3.**

# Keep Jetpack Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep your app classes - Preserve main functionality
-keep class com.example.secure.** { *; }

# Keep Lottie animations
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Keep Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# Keep PhotoView for image zooming
-keep class com.github.chrisbanes.photoview.** { *; }
-dontwarn com.github.chrisbanes.photoview.**

# Keep Android Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Keep Biometric classes
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Keep Navigation classes
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Keep ViewModel classes
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Preserve enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}