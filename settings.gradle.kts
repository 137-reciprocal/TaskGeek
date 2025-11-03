pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TaskHero"

// Main app module
include(":app")

// Feature modules
include(":feature:tasklist")
include(":feature:taskdetail")
include(":feature:reports")
include(":feature:settings")
include(":feature:filter")
include(":feature:hero")

// Domain modules
include(":domain:task")
include(":domain:filter")
include(":domain:report")
include(":domain:hero")
include(":domain:backup")
include(":domain:timetracking")

// Data modules
include(":data:task")
include(":data:preferences")
include(":data:hero")
include(":data:backup")
include(":data:timetracking")

// Core modules
include(":core:database")
include(":core:datastore")
include(":core:ui")
include(":core:common")
include(":core:notifications")
include(":core:parser")
include(":core:testing")

// Widget module
include(":widget")