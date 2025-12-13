plugins {
    id("thecolor-android-library-convention")
    id("com.mikepenz.aboutlibraries.plugin.android")
}

android {
    namespace = "io.github.mmolosay.thecolor.presentation.acknowledgements"
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
    val composeBom = platform("androidx.compose:compose-bom:${libs.versions.compose.bom.get()}")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.hilt:hilt-navigation-compose:${libs.versions.hiltNavigationCompose.get()}")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${libs.versions.androidx.lifecycle.get()}")

    // Hilt
    implementation("com.google.dagger:hilt-android:${libs.versions.hilt.get()}")
    ksp("com.google.dagger:hilt-compiler:${libs.versions.hilt.get()}")

    // Misc (preserve alphabetical order)
    implementation("com.mikepenz:aboutlibraries-core:${libs.versions.aboutLibraries.get()}")
    implementation("com.mikepenz:aboutlibraries-compose-m3:${libs.versions.aboutLibraries.get()}")
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