plugins {
    id("thecolor.android.library")
    id("thecolor.kotlin.android")
    id("thecolor.common")
    id("thecolor.hilt")
    id("thecolor.compose")
}

android {
    namespace = "io.github.mmolosay.thecolor.presentation.devoptions"
}

@Suppress("SpellCheckingInspection")
dependencies {
    // Modules
    implementation(project(":domain"))
    implementation(project(":utils"))
    implementation(project(":presentation:common"))
    implementation(project(":presentation:design-system"))

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")

    // Compose
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.hilt:hilt-navigation-compose:${libs.versions.hiltNavigationCompose.get()}")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${libs.versions.androidx.lifecycle.get()}")

    // Misc (preserve alphabetical order)
    implementation("io.github.mmolosay:debounce:${libs.versions.mmolosayDebounce.get()}")

    // Testing
    testImplementation(project(":utils:testing"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${libs.versions.junit.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("io.kotest:kotest-assertions-core:${libs.versions.kotestAssertions.get()}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
}