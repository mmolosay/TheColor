package io.github.mmolosay.thecolor.buildlogic.plugins

import io.github.mmolosay.thecolor.buildlogic.configureJava
import io.github.mmolosay.thecolor.buildlogic.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorKotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "org.jetbrains.kotlin.jvm") // also applies Java plugin, so Java configuration is needed
        target.configureJava()
        target.configureKotlin()
    }
}