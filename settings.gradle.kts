pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = uri("https://maven.fabricmc.net/"))
        maven(url = uri("https://maven.architectury.dev/"))
        maven(url = uri("https://maven.neoforged.net/releases/"))
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven(url = uri("https://maven.fabricmc.net/"))
        maven(url = uri("https://maven.architectury.dev/"))
        maven(url = uri("https://maven.neoforged.net/releases/"))
    }
}

rootProject.name = "GraalScript"

include("common")
include("fabric")
include("neoforge")