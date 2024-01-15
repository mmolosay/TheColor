plugins {
    id("com.android.application") version libs.versions.androidGradlePlugin.get() apply false
    id("com.android.library") version libs.versions.androidGradlePlugin.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("com.google.dagger.hilt.android") version libs.versions.hilt.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin.get() apply false
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.6")
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}