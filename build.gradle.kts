import org.gradle.api.tasks.Copy

plugins {
    java
    id("org.jetbrains.qodana") version "2025.2.1"
}

// Maven Central - https://repo1.maven.org/maven2/
project.extra["lwjglVersion"] = "3.3.6" // org.lwjgl:lwjgl, org.lwjgl:lwjgl-glfw, org.lwjgl:lwjgl-vulkan
project.extra["junitVersion"] = "6.0.0" // org.junit:junit-bom
project.extra["tinyLoggerVersion"] = "2.8.0-M1" // org.tinylog:tinylog-impl
project.extra["gsonVersion"] = "2.13.2" // com.google.code.gson:gson
project.extra["guavaVersion"] = "33.5.0-jre" // com.google.guava:guava

// FabricMC Maven - https://maven.fabricmc.net/
project.extra["fabricMixinVersion"] = "0.16.5+mixin.0.8.7" // net.fabricmc:sponge-mixin

// QuiltMC Maven - https://maven.quiltmc.org/repository/release/
project.extra["quiltLoaderVersion"] = "0.29.3-beta.1" // org.quiltmc:quilt-loader, org.quiltmc:quilt-loader-dependencies

project.extra["lwjglNativesNames"] = listOf(
    "natives-linux",
    "natives-linux-arm32",
    "natives-linux-arm64",
    "natives-linux-ppc64le",
    "natives-linux-riscv64",
    "natives-freebsd",
    "natives-macos",
    "natives-macos-arm64",
    "natives-windows",
    "natives-windows-x86",
    "natives-windows-arm64"
)

tasks.register<Copy>("preRun")
{
    from("$rootDir/installer")
    destinationDir = file("$rootDir/run")

    doLast {
        file("$rootDir/run/vulkan-glfw-render")
            .renameTo(file("$rootDir/run/modules"))
    }
}