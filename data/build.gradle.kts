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
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(project(":domain"))
    implementation(project(":utils"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
    implementation("javax.inject:javax.inject:1")
    implementation("com.github.ajalt.colormath:colormath:3.4.0")

    // Local
    val roomVersion = libs.versions.room.get()
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.datastore:datastore-preferences:${libs.versions.dataStore.preferences.get()}")

    // Remote
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.library.get()}")
    implementation("com.squareup.retrofit2:converter-moshi:${libs.versions.retrofit.moshi.get()}")

    // Testing
    testImplementation("junit:junit:${libs.versions.junit.get()}")
    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation("io.kotest:kotest-assertions-core:${libs.versions.kotestAssertions.get()}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
}