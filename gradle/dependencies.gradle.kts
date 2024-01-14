mapOf(
    // Android
    "compileSdkVersion" to 34,
    "targetSdkVersion" to 34,
    "minSdkVersion" to 24,
    "buildToolsVersion" to "34.0.0",

    // Kotlin
    "kotlinVersion" to "1.9.10",
    "coroutinesVersion" to "1.7.2",

    // Dagger
    "hiltVersion" to "2.48.1",
).entries.forEach {
    extra[it.key] = it.value
}