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

// Routing spike only: resolve BRouter's Java routing core directly from its pinned public source tag.
// This avoids a credentialed GitHub Packages dependency and keeps the prototype reproducible.
sourceControl {
    gitRepository(uri("https://github.com/abrensch/brouter.git")) {
        producesModule("org.btools:brouter-core")
    }
}

rootProject.name = "TrailCharter"
include(":app")
