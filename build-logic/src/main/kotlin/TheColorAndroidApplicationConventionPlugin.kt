import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import com.android.build.api.dsl.ApplicationExtension as AndroidApplicationExtension

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorAndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val libs = target.libs()

        target.apply(plugin = "com.android.application")

        target.extensions.configure<AndroidApplicationExtension> {
            compileSdk = libs.compileSdk
            defaultConfig {
                minSdk = libs.minSdk
                targetSdk = libs.targetSdk
            }
            buildTypes {
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                }
                debug {
                    isMinifyEnabled = false
                    isShrinkResources = false
                    applicationIdSuffix = ".debug"
                    versionNameSuffix = "-debug" // the final version name will looks like "1.0.7-debug"
                }
            }
        }
    }
}