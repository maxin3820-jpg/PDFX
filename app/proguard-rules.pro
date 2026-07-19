# Add project specific ProGuard rules here.

# ══════════════════════════════════════════════════════════════════════════════
# PDFX ProGuard Configuration — Safe Release Build
# ══════════════════════════════════════════════════════════════════════════════

# Keep attributes for debugging
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_ComponentTreeDeps { *; }
-keep class **_MembersInjector { *; }
-keep class **_Factory { *; }
-keep class **_Impl { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Keep all Room generated classes
-keep class com.pdfx.app.data.database.** { *; }

# ── Domain Models ─────────────────────────────────────────────────────────────
-keep class com.pdfx.app.domain.** { *; }
-keepclassmembers class com.pdfx.app.domain.** { *; }

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# ── Android PDF Renderer ──────────────────────────────────────────────────────
-keep class android.graphics.pdf.** { *; }
-keep class android.graphics.Bitmap { *; }

# ── DataStore Preferences ─────────────────────────────────────────────────────
-keep class androidx.datastore.*.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
  <fields>;
}

# ── Coil Image Loading ────────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ── Navigation Component ──────────────────────────────────────────────────────
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# ── ViewModel ─────────────────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# ── Keep Application & Activity ───────────────────────────────────────────────
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends androidx.activity.ComponentActivity

# ── Remove Logging (Performance) ──────────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# ── General Android ───────────────────────────────────────────────────────────
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── Optimization Settings ─────────────────────────────────────────────────────
-optimizationpasses 3
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Safe optimizations only
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
