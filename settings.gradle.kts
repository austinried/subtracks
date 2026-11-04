pluginManagement {
    val mavenCentralMirrorUrl = providers.gradleProperty("mavenCentralMirrorUrl").get()
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven(mavenCentralMirrorUrl) {
            name = "MavenCentralMirror"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven(providers.gradleProperty("mavenCentralMirrorUrl").get()) {
            name = "MavenCentralMirror"
        }
        mavenCentral()
    }
}

rootProject.name = "subtracks"
include(":app")
