plugins {
    id("thecolor.android.library")
    id("thecolor.kotlin.android")
    id("thecolor.common")
    id("thecolor.compose")
}

android {
    namespace = "io.github.mmolosay.thecolor.presentation.design"
}

dependencies {
    // Modules
    implementation(project(":domain"))

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")

    // Compose
    implementation("androidx.compose.material3:material3")

    // Misc (preserve alphabetical order)
    implementation("com.google.android.material:material:1.12.0") // for XML theme
    implementation("com.valentinilk.shimmer:compose-shimmer:${libs.versions.valentinilkShimmer.get()}")
}