plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "io.github.mmolosay.thecolor.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-proguard-rules.pro")
    }

    buildTypes {
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(project(":domain"))
    implementation(project(":utils"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
    implementation("javax.inject:javax.inject:1")

    // Local
    implementation("androidx.datastore:datastore-preferences:${libs.versions.dataStore.preferences.get()}")

    // Remote
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.library.get()}")
    implementation("com.squareup.moshi:moshi:1.15.2")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:${libs.versions.hilt.get()}") // for Dagger's Lazy<T>

    // Misc (preserve alphabetical order)
    implementation("com.github.ajalt.colormath:colormath:3.4.0")
    implementation("com.jakewharton.timber:timber:${libs.versions.jakewhartonTimber.get()}")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${libs.versions.junit.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("io.kotest:kotest-assertions-core:${libs.versions.kotestAssertions.get()}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
}