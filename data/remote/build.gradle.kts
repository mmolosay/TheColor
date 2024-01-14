plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

@Suppress("SpellCheckingInspection")
dependencies {
    implementation(project(":domain"))
    implementation(project(":utils"))

    // Retrofit
    api("com.squareup.retrofit2:retrofit:${libs.versions.retrofit.library.get()}")
    api("com.squareup.retrofit2:converter-moshi:${libs.versions.retrofit.moshi.get()}")
    api("com.squareup.okhttp3:logging-interceptor:${libs.versions.okhttp3.loggingInterceptor.get()}")
}