pluginManagement {
    repositories {
        // Default repositories
        gradlePluginPortal()
        mavenCentral()

        // Repositories
        maven("https://maven.unifycraft.xyz/releases")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://server.bbkr.space/artifactory/libs-release/")
        maven("https://jitpack.io/")

        // Snapshots
        maven("https://maven.unifycraft.xyz/snapshots")
        maven("https://s01.oss.sonatype.org/content/groups/public/")
        mavenLocal()
    }

    plugins {
        id("xyz.unifycraft.gradle.multiversion-root") version("1.11.1")
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
