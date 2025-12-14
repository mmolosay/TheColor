import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

@Suppress("unused") // registered in 'build.gradle.kts'
class TheColorCommonConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureJava()
        target.configureKotlin()
        target.configureTests()
    }
}

private fun Project.configureJava() {
    val libs = libs()
    fun configure() {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(libs.java))
            }
        }
    }
    // configure Java if it's a pure Java/Kotlin module
    plugins.withType<JavaPlugin> { configure() }
    // configure Java if it's an Android module: application or library
    plugins.withType<AndroidBasePlugin> { configure() }
}

private fun Project.configureKotlin() {
    val libs = libs()
    plugins.withType<KotlinBasePlugin> {
        extensions.configure<KotlinProjectExtension> {
            jvmToolchain(libs.java)
        }
    }
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            // https://github.com/Kotlin/kotlinx.serialization/issues/2145
            freeCompilerArgs.add("-Xstring-concat=inline")
            // https://youtrack.jetbrains.com/issue/KT-73255
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

private fun Project.configureTests() {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}