pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()

        maven("https://jitpack.io/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.sk1er.club/repository/maven-public/")
        maven("https://server.bbkr.space/artifactory/libs-release/")
    }

    plugins {
        id("xyz.unifycraft.gradle.multiversion-root") version("1.0.0")
    }
}

rootProject.name = "UniCore-Loader"

listOf(
    "1.8.9-forge"
).forEach { version ->
    listOf(
        "launchwrapper",
        "loader"
    ).forEach { stage ->
        include("$stage:$version")
        project(":$stage:$version").apply {
            projectDir = file("$stage/versions/$version")
            buildFileName = "../../version.gradle"
        }
    }
}
