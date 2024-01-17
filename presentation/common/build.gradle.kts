plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "io.github.mmolosay.thecolor.presentation"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    java {
        toolchain {
            val version = libs.versions.java.get().toInt()
            languageVersion.set(JavaLanguageVersion.of(version))
        }
    }
}

dependencies {

    implementation(project(":domain"))
    implementation(project(":presentation:design-system"))

    // Jetpack
    implementation("androidx.core:core-ktx:${libs.versions.androidx.core.coreKtx.get()}")
    implementation("androidx.appcompat:appcompat:${libs.versions.androidx.appcompat.get()}")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Material
    implementation("com.google.android.material:material:1.6.0-alpha02")

    // Third Party Libraries
    implementation("com.github.kirich1409:viewbindingpropertydelegate-noreflection:${libs.versions.viewbindingpropertydelegate.get()}")
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.12")
    /*
     * Was used for clipping color data with its custom background, but uses LAYER_TYPE_SOFTWARE,
     * and constant redrawing of such complex view is dramatic for performance.
     * implementation("io.github.florent37:shapeofview:1.4.7")
     */
    implementation("com.github.ajalt.colormath:colormath:2.1.0")
}