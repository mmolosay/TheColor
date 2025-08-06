plugins {
    id("java-library")
    id("kotlin")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xstring-concat=inline")
    }
}

java {
    toolchain {
        val version = libs.versions.java.get().toInt()
        languageVersion.set(JavaLanguageVersion.of(version))
    }
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${libs.versions.junit.get()}")
    implementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
}