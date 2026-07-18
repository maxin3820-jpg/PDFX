# Add project specific ProGuard rules here.

# ══════════════════════════════════════════════════════════════════════════════
# PDFX ProGuard Configuration — Optimized for Performance
# ══════════════════════════════════════════════════════════════════════════════

# Enable aggressive optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Optimization configuration
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Keep Room entities and DAOs
-keep class com.pdfx.app.data.database.** { *; }
-keepclassmembers class com.pdfx.app.data.database.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }

# Keep domain models (used across layers)
-keepclassmembers class com.pdfx.app.domain.model.** { *; }

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Android PdfRenderer (critical for PDF functionality)
-keep class android.graphics.pdf.** { *; }

# DataStore Preferences
-keep class androidx.datastore.*.** { *; }

# Remove logging in release builds for performance
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
