package io.github.mmolosay.thecolor.buildlogic.plugins

import io.github.mmolosay.thecolor.buildlogic.findAndroidExtension
import io.github.mmolosay.thecolor.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val libs = target.libs()

        require(target.plugins.hasPlugin("com.android.base")) // must be an Android module
        target.apply(plugin = "org.jetbrains.kotlin.plugin.compose")

        val androidExtension = requireNotNull(target.extensions.findAndroidExtension())
        androidExtension.apply {
            buildFeatures.apply {
                compose = true
            }
        }

//        target.enableComposeCompilerReports()

        target.dependencies {
            val bom = "androidx.compose:compose-bom:${libs.findVersion("compose.bom").get()}"
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("implementation", "androidx.compose.ui:ui-tooling-preview")
            add("debugImplementation", "androidx.compose.ui:ui-tooling")
        }
    }
}

@Suppress("unused")
private fun Project.enableComposeCompilerReports() {
    val extension = extensions.getByType<ComposeCompilerGradlePluginExtension>()
    extension.apply {
        val directory = layout.buildDirectory.dir("compose_compiler")
        reportsDestination.set(directory)
        metricsDestination.set(directory)
    }
}