pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TheColor"

include(":domain")
include(":utils")
include(":utils:testing")
include(":data")
include(":main")
include(":main:di-qualifiers")

include(":app")
include(":presentation:common")
include(":presentation:design-system")
include(":presentation:errors")

include(":presentation:home")
include(":presentation:color-input")
include(":presentation:color-input:testing")
include(":presentation:color-details")
include(":presentation:color-scheme")
include(":presentation:color-center")
include(":presentation:color-preview")
include(":presentation:settings")
include(":presentation:dev-options")
include(":presentation:acknowledgements")
