plugins {
    id("com.android.application") version libs.versions.androidGradlePlugin.get() apply false
    id("com.android.library") version libs.versions.androidGradlePlugin.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("com.google.dagger.hilt.android") version libs.versions.hilt.get() apply false
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin.get() apply false
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get() apply false
    id("org.jetbrains.kotlin.plugin.serialization") version libs.versions.kotlinSerialization.get() apply false
    id("tech.apter.junit5.jupiter.robolectric-extension-gradle-plugin") version libs.versions.robolectricJunit5Ext.get() apply false
    id("com.mikepenz.aboutlibraries.plugin.android") version libs.versions.aboutLibraries.get() apply false
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    configureJUnit()
}

private fun Project.configureJUnit() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}