import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorKotlinAndroidConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "org.jetbrains.kotlin.android") // also applies Java plugin, so Java configuration is needed
        target.configureJava()
        target.configureKotlin()
    }
}