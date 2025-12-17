plugins {
    id("thecolor.android.application")
    id("thecolor.kotlin.android")
    id("thecolor.common")
    id("thecolor.hilt")
    id("thecolor.compose")
}

android {
    namespace = "io.github.mmolosay.thecolor"
    defaultConfig {
        applicationId = "io.github.mmolosay.thecolor"
        versionCode = 1
        versionName = "1.0.0" // X.Y.Z; X = Major, Y = minor, Z = Patch level
    }
    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

@Suppress("SpellCheckingInspection")
dependencies {
    // Modules
    implementation(project(":domain"))
    implementation(project(":utils"))
    implementation(project(":main"))
    implementation(project(":presentation:design-system"))
    implementation(project(":presentation:common"))
    implementation(project(":presentation:home"))
    implementation(project(":presentation:settings"))
    implementation(project(":presentation:dev-options"))
    implementation(project(":presentation:acknowledgements"))

    // Jetpack
    implementation("androidx.appcompat:appcompat:${libs.versions.androidx.appcompat.get()}")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Compose
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-process:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.hilt:hilt-navigation-compose:${libs.versions.hiltNavigationCompose.get()}")

    // Misc (preserve alphabetical order)
    implementation("com.jakewharton.timber:timber:${libs.versions.jakewhartonTimber.get()}")
}