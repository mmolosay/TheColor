import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorAndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "com.android.application")
    }
}