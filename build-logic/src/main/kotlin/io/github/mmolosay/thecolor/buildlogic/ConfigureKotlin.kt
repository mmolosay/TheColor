package io.github.mmolosay.thecolor.buildlogic

import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal fun Project.configureKotlin() {
    val libs = libs()
    require(plugins.hasKotlinPlugin()) {
        "Kotlin plugin must be applied in order to configure Kotlin"
    }
    extensions.configure<KotlinProjectExtension> {
        jvmToolchain(libs.java)
    }
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            // https://github.com/Kotlin/kotlinx.serialization/issues/2145
            freeCompilerArgs.add("-Xstring-concat=inline")
            // https://youtrack.jetbrains.com/issue/KT-73255
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

private fun PluginContainer.hasKotlinPlugin(): Boolean {
    val kotlinPluginIds = listOf(
        "org.jetbrains.kotlin.jvm",
        "org.jetbrains.kotlin.android",
    )
    return kotlinPluginIds.any { id -> this.hasPlugin(id) }
}