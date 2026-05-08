pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
    "core:behaviorui",
    "core:tools",
    "feature:home",
    "feature:sub",
    "feature:stats",
    "feature:settings",
    "feature:categories",
    "feature:management_activities",
    "feature:tag_management",
    "feature:debug",
)
