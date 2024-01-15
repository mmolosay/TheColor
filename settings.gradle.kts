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

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "TheColor"
include(":domain")
include(":utils")
include(":data")
include(":data:local")
include(":data:remote")
include(":main")
include(":app")
include(":presentation:common")
include(":presentation:home")
