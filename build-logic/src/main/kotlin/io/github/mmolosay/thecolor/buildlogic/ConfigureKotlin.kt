package io.github.mmolosay.thecolor.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

internal fun Project.configureKotlin() {
    val libs = libs()
    extensions.configure<KotlinProjectExtension> {
        jvmToolchain(libs.java)
    }
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            // https://github.com/Kotlin/kotlinx.serialization/issues/2145
            freeCompilerArgs.add("-Xstring-concat=inline")
            // https://youtrack.jetbrains.com/issue/KT-73255
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
            // Arrow's optics employ context parameters, which require the explicit compiler opt-in
            // https://kotlinlang.org/docs/context-parameters.html
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}