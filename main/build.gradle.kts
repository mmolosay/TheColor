plugins {
    id("thecolor.android.library")
    id("thecolor.hilt")
}

android {
    namespace = "io.github.mmolosay.thecolor.main"
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))

    // Data Remote
    implementation("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.library.get()}")
    implementation("com.squareup.retrofit2:converter-moshi:${libs.versions.retrofit.moshi.get()}")
    implementation("com.squareup.okhttp3:logging-interceptor:${libs.versions.okhttp3.loggingInterceptor.get()}")

    // Data Local
    implementation("androidx.datastore:datastore-preferences:${libs.versions.dataStore.preferences.get()}")
}