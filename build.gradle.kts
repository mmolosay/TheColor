plugins {
    val agpV = "7.1.1" // TODO: 8.0.2
    id("com.android.application") version agpV apply false
    id("com.android.library") version agpV apply false
    id("com.google.devtools.ksp") version "1.8.22-1.0.11" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false
}

apply("dependencies.gradle")

buildscript {
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