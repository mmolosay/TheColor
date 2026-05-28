package io.github.mmolosay.thecolor.buildlogic.plugins

import io.github.mmolosay.thecolor.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorKotlinSerializationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val libs = target.libs()
        val kotlinSerializationCore = libs.findVersion("kotlinSerializationCore").get()

        target.apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
        target.dependencies.apply {
            add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinSerializationCore")
        }
    }
}