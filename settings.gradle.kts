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

// Routing spike only: resolve BRouter's Java routing modules directly from one pinned public source tag.
// BRouter keeps these as Gradle implementation dependencies internally, while its public routing
// classes expose mapaccess supertypes, so TrailCharter must resolve the five modules explicitly.
sourceControl {
    gitRepository(uri("https://github.com/abrensch/brouter.git")) {
        producesModule("org.btools:brouter-core")
        producesModule("org.btools:brouter-mapaccess")
        producesModule("org.btools:brouter-util")
        producesModule("org.btools:brouter-expressions")
        producesModule("org.btools:brouter-codec")
    }
}

rootProject.name = "TrailCharter"
include(":app")
