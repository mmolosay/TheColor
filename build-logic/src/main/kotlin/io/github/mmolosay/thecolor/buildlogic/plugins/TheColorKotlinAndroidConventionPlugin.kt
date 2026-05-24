package io.github.mmolosay.thecolor.buildlogic.plugins

import io.github.mmolosay.thecolor.buildlogic.configureJava
import io.github.mmolosay.thecolor.buildlogic.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorKotlinAndroidConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Gradle 9.0+ adds built-in Kotlin dependency
        target.configureJava()
        target.configureKotlin()
    }
}