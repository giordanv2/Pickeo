pluginManagement {
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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pickeo"
include(":app")
include(":core:database")
include(":feature:catalog:catalog-feat")
include(":feature:catalog:catalog-lib")
include(":feature:cart:cart-feat")
include(":feature:cart:cart-lib")
include(":feature:order-entry")
