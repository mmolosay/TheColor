plugins {
    id("java-library")
    id("kotlin")
}

java {
    toolchain {
        val version = libs.versions.java.get().toInt()
        languageVersion.set(JavaLanguageVersion.of(version))
    }
}

dependencies {
    implementation(project(":utils"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
    implementation("javax.inject:javax.inject:1")
}