package io.github.mmolosay.thecolor.buildlogic.plugins

import io.github.mmolosay.thecolor.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val libs = target.libs()
        val hiltVersion = libs.findVersion("hilt").get()
        val kotlinVersion = libs.findVersion("kotlin").get()

        require(target.plugins.hasPlugin("com.android.base")) // must be an Android module

        target.apply(plugin = "com.google.devtools.ksp")
        target.apply(plugin = "dagger.hilt.android.plugin")

        target.dependencies {
            add("ksp", "com.google.dagger:hilt-compiler:$hiltVersion")
            add("ksp", "org.jetbrains.kotlin:kotlin-metadata-jvm:$kotlinVersion")
            add("implementation", "com.google.dagger:hilt-android:$hiltVersion")
        }
    }
}