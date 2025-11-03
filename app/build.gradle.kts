plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("androidx.baselineprofile")
}

android {
    namespace = "com.taskhero"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.taskhero"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    // Baseline Profile configuration
    baselineProfile {
        // Automatically generate baseline profiles during build
        automaticGenerationDuringBuild = false
        // Use the baseline profile from src/main/baseline-prof.txt
        baselineProfileOutputDir = "src/main"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Feature modules
    implementation(project(":feature:tasklist"))
    implementation(project(":feature:taskdetail"))
    implementation(project(":feature:reports"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:filter"))
    implementation(project(":feature:hero"))

    // Domain modules
    implementation(project(":domain:task"))
    implementation(project(":domain:filter"))
    implementation(project(":domain:report"))
    implementation(project(":domain:hero"))
    implementation(project(":domain:timetracking"))

    // Data modules
    implementation(project(":data:task"))
    implementation(project(":data:preferences"))
    implementation(project(":data:hero"))
    implementation(project(":data:timetracking"))

    // Core modules
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
    implementation(project(":core:notifications"))

    // Widget
    implementation(project(":widget"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material3.window.size)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
