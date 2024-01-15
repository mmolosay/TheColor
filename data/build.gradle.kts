plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "io.github.mmolosay.thecolor.data"
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
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":utils"))
    implementation(project(":data:local"))
    implementation(project(":data:remote"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
    implementation("javax.inject:javax.inject:1")
}