plugins {
    `kotlin-dsl`
}

kotlin {
    val javaVersion = libs.versions.java.get().toInt()
    jvmToolchain(javaVersion)
}

dependencies {
    implementation("com.android.tools:common:${libs.versions.tools.get()}") // Android variant configuration helpers and other

    // Plugins
    implementation("com.android.tools.build:gradle-api:${libs.versions.androidGradlePlugin.get()}") // "com.android.application" and "com.android.library"
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}") // "org.jetbrains.kotlin.android"
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:${libs.versions.kotlinSerializationPlugin.get()}")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}") // "org.jetbrains.kotlin.plugin.compose"
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}") // "com.google.devtools.ksp"
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("TheColorCommon") {
            id = "thecolor.common"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorCommonConventionPlugin"
        }
        register("TheColorKotlinJvm") {
            id = "thecolor.kotlin.jvm"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorKotlinJvmConventionPlugin"
        }
        register("TheColorKotlinAndroid") {
            id = "thecolor.kotlin.android"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorKotlinAndroidConventionPlugin"
        }
        register("TheColorKotlinSerialization") {
            id = "thecolor.kotlin.serialization"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorKotlinSerializationConventionPlugin"
        }
        register("TheColorAndroidApplication") {
            id = "thecolor.android.application"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorAndroidApplicationConventionPlugin"
        }
        register("TheColorAndroidLibrary") {
            id = "thecolor.android.library"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorAndroidLibraryConventionPlugin"
        }
        register("TheColorCompose") {
            id = "thecolor.compose"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorComposeConventionPlugin"
        }
        register("TheColorHilt") {
            id = "thecolor.hilt"
            implementationClass = "io.github.mmolosay.thecolor.buildlogic.plugins.TheColorHiltConventionPlugin"
        }
    }
}