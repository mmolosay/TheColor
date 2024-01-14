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
include(":app")
include(":domain")
include(":data")
include(":data:local")
include(":data:remote")
include(":feature:home")
include(":utils")
include(":main")
