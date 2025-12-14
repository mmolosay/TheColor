import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal fun Project.libs(): VersionCatalog =
    this.extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val VersionCatalog.compileSdk: Int
    get() = this.findVersion("compileSdk").get()
        .toString().toInt()

internal val VersionCatalog.minSdk: Int
    get() = this.findVersion("minSdk").get()
        .toString().toInt()