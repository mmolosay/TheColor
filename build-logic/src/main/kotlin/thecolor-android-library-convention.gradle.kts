val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    // 'namespace' has to be defined in a client module
    compileSdk = libs.findVersion("compileSdk").get().toString().toInt()

    defaultConfig {
        minSdk = libs.findVersion("minSdk").get().toString().toInt()
    }

    buildFeatures {
        compose = true
    }
}