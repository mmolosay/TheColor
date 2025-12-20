package io.github.mmolosay.thecolor.buildlogic

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure

internal fun Project.configureJava() {
    val libs = libs()
    require(plugins.hasPlugin(JavaBasePlugin::class.java)) {
        "Java plugin must be applied in order to configure Java"
    }
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.java))
        }
    }
}