plugins {
    id("java-library")
    id("kotlin")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}