@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "io.github.mmolosay.thecolor.main"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    java {
        toolchain {
            val version = libs.versions.java.get().toInt()
            languageVersion.set(JavaLanguageVersion.of(version))
        }
    }

    kapt {
        correctErrorTypes = true
    }
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":data:local"))
    implementation(project(":data:remote"))

    // Data Remote
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.library.get()}")
    implementation("com.squareup.retrofit2:converter-moshi:${libs.versions.retrofit.moshi.get()}")
    implementation("com.squareup.okhttp3:logging-interceptor:${libs.versions.okhttp3.loggingInterceptor.get()}")

    // Data Local
    implementation("androidx.room:room-runtime:${libs.versions.room.get()}")

    // Hilt
    implementation("com.google.dagger:hilt-android:${libs.versions.hilt.get()}")
    kapt("com.google.dagger:hilt-compiler:${libs.versions.hilt.get()}")
}