pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
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
    "feature:timer"
)
