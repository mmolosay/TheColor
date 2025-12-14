import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import com.android.build.gradle.LibraryExtension as AndroidLibraryExtension

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorAndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val libs = target.libs()

        target.apply(plugin = "com.android.library")
        target.apply(plugin = "org.jetbrains.kotlin.android")

        target.extensions.configure<AndroidLibraryExtension> {
            compileSdk = libs.compileSdk
            defaultConfig {
                minSdk = libs.minSdk
                // unspecified targetSdk in library modules will inherit the targetSdk of the application
            }
        }
    }
}