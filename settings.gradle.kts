rootProject.name = "Pomtom"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":composeApp")

include(":shared:domain")

include(":shared:core:common")
include(":shared:core:designsystem")
include(":shared:core:database")
include(":shared:core:datastore")
include(":shared:core:audio")
include(":shared:core:filepicker")
include(":shared:core:platform")

include(":shared:feature:onboarding")
include(":shared:feature:timer")
include(":shared:feature:goals")
include(":shared:feature:audio")
include(":shared:feature:stats")
include(":shared:feature:settings")
