package io.github.mmolosay.thecolor.buildlogic.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorCommonConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureTests()
    }
}

private fun Project.configureTests() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}