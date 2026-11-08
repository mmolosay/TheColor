plugins {
    id("thecolor.android.application")
    id("thecolor.kotlin.android")
    id("thecolor.common")
    id("thecolor.hilt")
    id("thecolor.compose")
    id("tech.apter.junit5.jupiter.robolectric-extension-gradle-plugin")
}

android {
    namespace = "io.github.mmolosay.thecolor.demo"
    defaultConfig {
        applicationId = "io.github.mmolosay.thecolor.demo"
        versionCode = 1
        versionName = "1.0.0"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true // for Compose UI tests: the activity and the strings
        }
    }
}

@Suppress("SpellCheckingInspection")
dependencies {
    // Modules
    implementation(project(":composition"))
    implementation(project(":main:di-qualifiers"))
    implementation(project(":presentation:design-system"))
    implementation(project(":utils"))

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")

    // Compose
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${libs.versions.androidx.lifecycle.get()}")
    implementation("androidx.hilt:hilt-navigation-compose:${libs.versions.hiltNavigationCompose.get()}")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${libs.versions.junit.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${libs.versions.junit.get()}")
    testImplementation("io.kotest:kotest-assertions-core:${libs.versions.kotestAssertions.get()}")
    testImplementation(project(":utils:testing"))
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
    testImplementation("org.robolectric:robolectric:${libs.versions.robolectric.get()}")
    testImplementation("androidx.compose.ui:ui-test")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
