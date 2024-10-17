plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "io.github.mmolosay.thecolor.presentation.design"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
    }

    java {
        toolchain {
            val version = libs.versions.java.get().toInt()
            languageVersion.set(JavaLanguageVersion.of(version))
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    // Modules
    implementation(project(":domain"))

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:${libs.versions.compose.bom.get()}")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    implementation("com.google.android.material:material:1.12.0") // for XML theme
}