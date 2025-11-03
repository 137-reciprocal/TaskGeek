plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.taskhero.core.testing"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // JUnit
    api(libs.junit)

    // Mockk
    api(libs.mockk)

    // Turbine
    api(libs.turbine)

    // Kotlin Coroutines Test
    api(libs.kotlinx.coroutines.test)

    // Testing
    androidTestImplementation(libs.androidx.test.runner)
}
