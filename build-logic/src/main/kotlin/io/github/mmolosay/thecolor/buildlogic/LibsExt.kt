package io.github.mmolosay.thecolor.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal fun Project.libs(): VersionCatalog =
    this.extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val VersionCatalog.compileSdk: Int
    get() = this.findVersion("compileSdk").get().toString().toInt()

internal val VersionCatalog.minSdk: Int
    get() = this.findVersion("minSdk").get().toString().toInt()

internal val VersionCatalog.targetSdk: Int
    get() = this.findVersion("targetSdk").get().toString().toInt()

internal val VersionCatalog.java: Int
    get() = this.findVersion("java").get().toString().toInt()