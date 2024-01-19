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
include(":data:local")
include(":data:remote")
include(":main")
include(":app")
include(":presentation:common")
include(":presentation:design-system")
include(":presentation:home")
include(":presentation:color-input")
