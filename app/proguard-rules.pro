# TaskHero ProGuard Rules

# General Android rules
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization flags
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep source file and line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*,Signature,Exception,InnerClasses,EnclosingMethod

# ========================================
# Hilt / Dagger
# ========================================
-dontwarn com.google.errorprone.annotations.*

# Keep Hilt generated classes
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt components and modules
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **_ComponentTreeDeps { *; }
-keep class **_Impl { *; }
-keep class **_Impl$** { *; }

# Hilt view models
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ========================================
# Room Database
# ========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep @androidx.room.Database class *

# Keep Room generated classes
-keep class **_Impl { *; }
-keep class **$Companion { *; }

# Keep entity classes and their fields
-keep class com.taskhero.core.database.entity.** { *; }
-keepclassmembers class com.taskhero.core.database.entity.** { *; }

# Keep DAO interfaces
-keep interface com.taskhero.core.database.dao.** { *; }

# Keep database class
-keep class com.taskhero.core.database.TaskHeroDatabase { *; }
-keep class com.taskhero.core.database.TaskHeroDatabase_Impl { *; }

# ========================================
# Kotlin Serialization
# ========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Serializers
-keep,includedescriptorclasses class com.taskhero.**$$serializer { *; }
-keepclassmembers class com.taskhero.** {
    *** Companion;
}
-keepclasseswithmembers class com.taskhero.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializable classes
-keep @kotlinx.serialization.Serializable class ** { *; }

# ========================================
# Data Classes
# ========================================
# Keep all model/data classes used in serialization or database
-keep class com.taskhero.core.common.model.** { *; }
-keep class com.taskhero.domain.**.model.** { *; }
-keep class com.taskhero.feature.**.*.UiState { *; }
-keep class com.taskhero.feature.**.*.Intent { *; }
-keep class com.taskhero.feature.**.*.Effect { *; }

# Keep data class copy methods and component functions
-keepclassmembers class com.taskhero.** {
    public ** copy(...);
    public ** component1();
    public ** component2();
    public ** component3();
    public ** component4();
    public ** component5();
    public ** component6();
    public ** component7();
    public ** component8();
    public ** component9();
}

# ========================================
# Jetpack Compose
# ========================================
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep Composable functions
-keepclasseswithmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep CompositionLocal classes
-keep class androidx.compose.runtime.CompositionLocal { *; }
-keep class androidx.compose.runtime.ProvidedValue { *; }

# Keep remember and state functions
-keep class androidx.compose.runtime.RememberKt { *; }
-keep class androidx.compose.runtime.SnapshotStateKt { *; }

# ========================================
# Navigation
# ========================================
-keep class androidx.navigation.** { *; }
-keep class * extends androidx.navigation.Navigator { *; }

# Keep navigation arguments
-keepclassmembers class * {
    @androidx.navigation.** *;
}

# ========================================
# Coroutines
# ========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ========================================
# Kotlin
# ========================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { *; }

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep companion objects
-keepclassmembers class ** {
    ** Companion;
}

# ========================================
# AndroidX and Material Components
# ========================================
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }

-dontwarn androidx.**
-dontwarn com.google.android.material.**

# ========================================
# Application Classes
# ========================================
# Keep Application class
-keep class com.taskhero.TaskHeroApplication { *; }

# Keep MainActivity
-keep class com.taskhero.MainActivity { *; }

# Keep all ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ========================================
# Widget Classes
# ========================================
-keep class com.taskhero.widget.** { *; }
-keep class * extends android.appwidget.AppWidgetProvider { *; }

# ========================================
# Notifications
# ========================================
-keep class com.taskhero.core.notifications.** { *; }

# ========================================
# DataStore
# ========================================
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# ========================================
# Enum Classes
# ========================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========================================
# Parcelable
# ========================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ========================================
# Native Methods
# ========================================
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# ========================================
# Debugging (Remove in production)
# ========================================
# Uncomment to keep class names for better crash reports
# -keepnames class ** { *; }

# Uncomment to keep line numbers for crash reports
# -keepattributes SourceFile,LineNumberTable

# ========================================
# Miscellaneous
# ========================================
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# Keep generic signatures for reflection
-keepattributes Signature

# Keep BuildConfig
-keep class com.taskhero.BuildConfig { *; }

# Keep R classes
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ========================================
# Obfuscation Settings
# ========================================
# Use unique class member names
-useuniqueclassmembernames

# Don't obfuscate classes that are used with reflection
-keepattributes *Annotation*

# Keep names of fields annotated with specific annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.PrimaryKey <fields>;
}
