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
    implementation("com.android.tools.build:gradle:${libs.versions.androidGradlePlugin.get()}") // "com.android.application" and "com.android.library"
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}") // "org.jetbrains.kotlin.android"
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}") // "org.jetbrains.kotlin.plugin.compose"
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}") // "com.google.devtools.ksp"
    implementation("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}") // "com.google.dagger.hilt.android"
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