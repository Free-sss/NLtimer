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

rootProject.name = "NLtimer"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app",
    "core:designsystem",
    "core:data",
    "feature:home",
    "feature:sub",
    "feature:stats",
    "feature:settings",
    "feature:categories",
)
