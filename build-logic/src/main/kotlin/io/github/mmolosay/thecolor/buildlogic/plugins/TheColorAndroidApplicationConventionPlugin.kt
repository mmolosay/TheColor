package io.github.mmolosay.thecolor.buildlogic.plugins

import io.github.mmolosay.thecolor.buildlogic.compileSdk
import io.github.mmolosay.thecolor.buildlogic.libs
import io.github.mmolosay.thecolor.buildlogic.minSdk
import io.github.mmolosay.thecolor.buildlogic.targetSdk
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import com.android.build.api.dsl.ApplicationExtension as AndroidApplicationExtension

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorAndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val libs = target.libs()

        target.apply(plugin = "com.android.application")

        target.extensions.configure<AndroidApplicationExtension> {
            compileSdk = libs.compileSdk
            defaultConfig.apply {
                minSdk = libs.minSdk
                targetSdk = libs.targetSdk
            }
            buildTypes {
                release {
                    isDebuggable = false
                    isMinifyEnabled = true
                    isShrinkResources = true
                }
                debug {
                    isDebuggable = true
                    isMinifyEnabled = false
                    isShrinkResources = false
                    applicationIdSuffix = ".debug"
                    versionNameSuffix = "-debug" // e.g. "1.0.7-debug"
                }
                create("qa") {
                    isDebuggable = false
                    signingConfig = signingConfigs.getByName("debug")
                    matchingFallbacks += listOf("release")
                    isMinifyEnabled = false
                    isShrinkResources = false
                    applicationIdSuffix = ".qa"
                    versionNameSuffix = "-qa" // e.g. "1.0.7-qa"
                }
            }
        }
    }
}