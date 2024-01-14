plugins {
    // TODO: take versions from dependencies.gradle or other source
    val agpV = "7.1.1" // TODO: 8.0.2
    id("com.android.application") version agpV apply false
    id("com.android.library") version agpV apply false
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}

apply("dependencies.gradle")

buildscript {
    apply("/gradle/dependencies.gradle.kts")
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.0-alpha01")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}