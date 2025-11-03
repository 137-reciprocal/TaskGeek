# TaskHero - Build & Run Guide

Quick reference for building and running the TaskHero Android application.

---

## 📋 Prerequisites

### Required Software

| Software | Minimum Version | Download Link |
|----------|----------------|---------------|
| **Android Studio** | Ladybug 2024.2+ | [Download](https://developer.android.com/studio) |
| **JDK** | 17 | Bundled with Android Studio |
| **Android SDK** | API 36 (Android 16) | Via Android Studio SDK Manager |
| **Git** | Any recent version | [Download](https://git-scm.com/) |

### System Requirements

- **OS:** Windows 10/11, macOS 10.14+, or Linux
- **RAM:** 8 GB minimum (16 GB recommended)
- **Disk Space:** 10 GB free space
- **Internet:** Required for downloading dependencies

---

## 🚀 Quick Start (5 Minutes)

### 1. Open Project in Android Studio

```bash
# Option A: From command line
cd /path/to/TaskHero
open -a "Android Studio" .

# Option B: Or use Android Studio UI
# File → Open → Select TaskHero folder
```

### 2. Sync Gradle

Android Studio will automatically prompt to sync. Click **"Sync Now"**.

If not prompted:
```
File → Sync Project with Gradle Files
```

### 3. Connect Device or Start Emulator

**Physical Device:**
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect via USB
4. Accept debugging prompt on device

**Emulator:**
1. Tools → Device Manager
2. Create Device (Pixel 10 Pro XL, API 36 recommended)
3. Click Play button to start

### 4. Run the App

```
Run → Run 'app'
```

Or press `⇧F10` (Shift+F10)

---

## 🛠️ Build Commands

### Using Android Studio

| Task | Menu | Shortcut |
|------|------|----------|
| **Run Debug** | Run → Run 'app' | ⇧F10 |
| **Run Release** | Build → Select Build Variant → release | - |
| **Clean Build** | Build → Clean Project | - |
| **Rebuild** | Build → Rebuild Project | - |
| **Make Project** | Build → Make Project | ⌘F9 (Mac) / Ctrl+F9 (Win) |

### Using Gradle (Command Line)

```bash
# Make gradlew executable (first time only on Unix)
chmod +x gradlew

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Install and run
./gradlew installDebug && adb shell am start -n com.taskhero.debug/.MainActivity

# Run all unit tests
./gradlew test

# Run instrumented tests (requires connected device)
./gradlew connectedAndroidTest

# Build and test everything
./gradlew build

# Generate test coverage report
./gradlew jacocoTestReport
```

---

## 📦 Build Outputs

After building, find APKs here:

```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

---

## 🔧 Troubleshooting

### Problem: Gradle Sync Failed

**Solution:**
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/
rm -rf .gradle/

# Re-sync
./gradlew clean build --refresh-dependencies
```

### Problem: SDK Not Found

**Solution:**
1. Open Android Studio → Settings
2. Appearance & Behavior → System Settings → Android SDK
3. Install Android 16 (API 36)
4. Apply changes

### Problem: Build Too Slow

**Solutions:**
```properties
# Add to gradle.properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

### Problem: Out of Memory

**Solution:**
```properties
# Increase Gradle heap size in gradle.properties
org.gradle.jvmargs=-Xmx8192m -Dfile.encoding=UTF-8
```

### Problem: Hilt Compilation Errors

**Solution:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build --no-build-cache
```

### Problem: Compose Preview Not Working

**Solution:**
1. Build → Make Project
2. Invalidate Caches: File → Invalidate Caches → Invalidate and Restart

---

## 🧪 Testing

### Run All Tests

```bash
# Unit tests only
./gradlew test

# Instrumented tests (requires device)
./gradlew connectedAndroidTest

# Specific module tests
./gradlew :app:test
./gradlew :feature:tasklist:test

# With coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Test Reports Location

```
build/reports/tests/testDebugUnitTest/index.html
build/reports/androidTests/connected/index.html
build/reports/jacoco/jacocoTestReport/html/index.html
```

---

## 📱 Running on Different Devices

### Pixel 10 Pro XL (Target Device)

```bash
# Create AVD via command line
avdmanager create avd -n Pixel_10_Pro_XL -k "system-images;android-36;google_apis;x86_64" -d "pixel_10_pro_xl"

# Or use Android Studio Device Manager
```

### Physical Device

```bash
# Check connected devices
adb devices

# Install to specific device
adb -s DEVICE_SERIAL install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep "TaskHero"
```

### Multiple Devices

```bash
# Install on all connected devices
adb devices | tail -n +2 | cut -sf 1 | xargs -I {} adb -s {} install app-debug.apk
```

---

## 🎨 Build Variants

### Debug Build

- Includes debug information
- Application ID: `com.taskhero.debug`
- Debuggable: Yes
- Minification: No

### Release Build

- Optimized for production
- Application ID: `com.taskhero`
- Debuggable: No
- Minification: Yes (when ProGuard configured)

### Switch Build Variant

```
View → Tool Windows → Build Variants
→ Select "debug" or "release"
```

---

## 📊 Performance Profiling

### Using Android Studio Profiler

1. Run app in debug mode
2. View → Tool Windows → Profiler
3. Select process: `com.taskhero.debug`
4. Profile CPU, Memory, Network, or Energy

### Generate Baseline Profile

```bash
# This will be automated when baseline profiles are added
./gradlew generateBaselineProfile
```

---

## 🔒 Signing Release APK

### Create Keystore (First Time Only)

```bash
keytool -genkey -v -keystore taskhero-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias taskhero
```

### Sign APK

Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../taskhero-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "taskhero"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

Build signed APK:

```bash
export KEYSTORE_PASSWORD=your_password
export KEY_PASSWORD=your_password
./gradlew assembleRelease
```

---

## 📦 Creating AAB (Android App Bundle)

For Google Play Store submission:

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

---

## 🐛 Debugging

### Enable Verbose Logging

In `MainActivity.kt`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (BuildConfig.DEBUG) {
        Timber.plant(Timber.DebugTree())
    }
    // ...
}
```

### View Database

```bash
# Pull database from device
adb exec-out run-as com.taskhero.debug cat databases/taskhero_database > taskhero.db

# Open with sqlite3
sqlite3 taskhero.db
.tables
SELECT * FROM tasks;
```

### Clear App Data

```bash
adb shell pm clear com.taskhero.debug
```

---

## 📝 Useful ADB Commands

```bash
# View app info
adb shell dumpsys package com.taskhero.debug

# Force stop app
adb shell am force-stop com.taskhero.debug

# Start app
adb shell am start -n com.taskhero.debug/.MainActivity

# View shared preferences
adb shell run-as com.taskhero.debug cat shared_prefs/taskhero_preferences.xml

# Take screenshot
adb exec-out screencap -p > screenshot.png

# Record screen
adb shell screenrecord /sdcard/demo.mp4
# Press Ctrl+C to stop, then pull:
adb pull /sdcard/demo.mp4
```

---

## 🚢 CI/CD Setup (Future)

### GitHub Actions Example

```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Build with Gradle
        run: ./gradlew build
      - name: Run Tests
        run: ./gradlew test
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## 📚 Additional Resources

- [Android Studio User Guide](https://developer.android.com/studio/intro)
- [Gradle Build Documentation](https://developer.android.com/build)
- [ADB Documentation](https://developer.android.com/tools/adb)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io)

---

## ⚡ Quick Reference

### Essential Commands

```bash
# Build and install
./gradlew installDebug

# Clean and rebuild
./gradlew clean build

# Run tests
./gradlew test

# View logs
adb logcat | grep TaskHero

# Clear data and restart
adb shell pm clear com.taskhero.debug && \
adb shell am start -n com.taskhero.debug/.MainActivity
```

---

**Last Updated:** November 3, 2025
**Project:** TaskHero Android App
**Build System:** Gradle 8.11.1
