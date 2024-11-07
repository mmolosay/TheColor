pluginManagement {
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

include(":app")
include(":presentation:common:api")
include(":presentation:common:impl")
include(":presentation:design-system")
include(":presentation:errors")

include(":presentation:home")
include(":presentation:color-input:api")
include(":presentation:color-input:impl")
include(":presentation:color-details")
include(":presentation:color-scheme")
include(":presentation:color-center")
include(":presentation:color-preview")
include(":presentation:settings")
