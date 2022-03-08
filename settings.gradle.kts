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
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.replaymod.preprocess" -> useModule("com.github.replaymod:preprocessor:${requested.version}")
                "com.replaymod.preprocess-root" -> useModule("com.github.replaymod:preprocessor:${requested.version}")
            }
        }
    }
}

rootProject.name = "UniCore-Loader"

listOf(
    "1.8.9"
).forEach { version ->
    include(":stage0:$version")
    include(":stage1:$version")
    include(":stage2:$version")

    project(":stage0:$version").apply {
        projectDir = file("stage0/versions/$version")
        buildFileName = "../../version.gradle"
    }
    project(":stage1:$version").apply {
        projectDir = file("stage1/versions/$version")
        buildFileName = "../../version.gradle"
    }
    project(":stage2:$version").apply {
        projectDir = file("stage2/versions/$version")
        buildFileName = "../../version.gradle"
    }
}
