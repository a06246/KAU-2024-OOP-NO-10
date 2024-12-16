pluginManagement {
    repositories {
        google() // Google 저장소 추가
        mavenCentral() // Maven Central 저장소 추가
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google() // Google 저장소 추가
        mavenCentral() // Maven Central 저장소 추가
        maven("https://jitpack.io")
    }
}

rootProject.name = "SharedBudget"
include(":app")
