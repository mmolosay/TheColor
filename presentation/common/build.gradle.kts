plugins {
    id("thecolor.android.library")
    id("thecolor.kotlin.android")
    id("thecolor.common")
    id("thecolor.hilt")
    id("thecolor.compose")
    id("tech.apter.junit5.jupiter.robolectric-extension-gradle-plugin")
}

android {
    namespace = "io.github.mmolosay.thecolor.presentation.common"
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(project(":domain"))
    implementation(project(":utils"))
    implementation(project(":presentation:design-system"))

    // Compose
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.hilt:hilt-navigation-compose:${libs.versions.hiltNavigationCompose.get()}")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${libs.versions.androidx.lifecycle.get()}")

    // Misc (preserve alphabetical order)
    implementation("com.jakewharton.timber:timber:${libs.versions.jakewhartonTimber.get()}")

    // Testing
    testImplementation(project(":utils:testing"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${libs.versions.junit.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${libs.versions.junit.get()}")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("io.kotest:kotest-assertions-core:${libs.versions.kotestAssertions.get()}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
    testImplementation("org.robolectric:robolectric:${libs.versions.robolectric.get()}")
}