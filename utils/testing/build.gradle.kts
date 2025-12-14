plugins {
    id("thecolor.jvm.library")
    id("thecolor.common")
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:${libs.versions.junit.get()}")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${libs.versions.junit.get()}")
    implementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${libs.versions.coroutines.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
    implementation("io.mockk:mockk:${libs.versions.mockk.get()}")
}