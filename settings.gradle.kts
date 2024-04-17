pluginManagement {
    includeBuild("bundletool-plugin")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        //maven {
        //    setUrl("bundletool-plugin/plugin/build/repo")
        //}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BundleToolPlugin"
include(":app")
include(":dynamic_feature_one")
include(":dynamic_feature_two")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}