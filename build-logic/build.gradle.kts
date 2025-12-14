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
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}") // "com.google.devtools.ksp"
    implementation("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}") // "com.google.dagger.hilt.android"
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}") // "org.jetbrains.kotlin.plugin.compose"
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
            implementationClass = "TheColorCommonConventionPlugin"
        }
        register("TheColorJvmLibrary") {
            id = "thecolor.jvm.library"
            implementationClass = "TheColorJvmLibraryConventionPlugin"
        }
        register("TheColorAndroidApplication") {
            id = "thecolor.android.application"
            implementationClass = "TheColorAndroidApplicationConventionPlugin"
        }
        register("TheColorAndroidLibrary") {
            id = "thecolor.android.library"
            implementationClass = "TheColorAndroidLibraryConventionPlugin"
        }
        register("TheColorCompose") {
            id = "thecolor.compose"
            implementationClass = "TheColorComposeConventionPlugin"
        }
        register("TheColorHilt") {
            id = "thecolor.hilt"
            implementationClass = "TheColorHiltConventionPlugin"
        }
    }
}