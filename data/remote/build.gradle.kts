plugins {
    id("java-library")
    id("kotlin")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(project(":domain"))
    implementation(project(":utils"))

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.library.get()}")
    implementation("com.squareup.retrofit2:converter-moshi:${libs.versions.retrofit.moshi.get()}")
}