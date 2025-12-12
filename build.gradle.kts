import com.android.build.gradle.api.AndroidBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("com.android.application") version libs.versions.androidGradlePlugin.get() apply false
    id("com.android.library") version libs.versions.androidGradlePlugin.get() apply false
    id("com.google.devtools.ksp") version libs.versions.ksp.get() apply false
    id("com.google.dagger.hilt.android") version libs.versions.hilt.get() apply false
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin.get() apply false
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get() apply false
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
    configureJavaForAllPlugins()
    configureKotlinForAllPlugins()
    configureJUnit()
}

/**
 * Configures Java for all plugins that use it.
 */
private fun Project.configureJavaForAllPlugins() {
    fun JavaPluginExtension.configure() {
        toolchain {
            val version = libs.versions.java.get().toInt()
            languageVersion.set(JavaLanguageVersion.of(version))
        }
    }
    // configure Java for Android modules: applications and libraries
    plugins.withType<AndroidBasePlugin> {
        extensions.configure<JavaPluginExtension> { configure() }
    }
    // configure java for pure Java/Kotlin modules
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> { configure() }
    }
}

/**
 * Configures Kotlin for all plugins that use it.
 */
private fun Project.configureKotlinForAllPlugins() {
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xstring-concat=inline", // https://github.com/Kotlin/kotlinx.serialization/issues/2145
                "-Xannotation-default-target=param-property", // https://youtrack.jetbrains.com/issue/KT-73255
            )
        }
    }
}

private fun Project.configureJUnit() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}