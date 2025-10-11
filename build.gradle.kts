plugins {
    java
    id("org.jetbrains.qodana") version "2025.2.1"
}

// Maven Central - https://repo1.maven.org/maven2/
project.extra["lwjglVersion"] = "3.3.6" // org.lwjgl:lwjgl, org.lwjgl:lwjgl-glfw, org.lwjgl:lwjgl-vulkan
project.extra["tinyLoggerVersion"] = "2.8.0-M1" // org.tinylog:tinylog-impl
project.extra["gsonVersion"] = "2.13.2" // com.google.code.gson:gson
project.extra["guavaVersion"] = "33.5.0-jre" // com.google.guava:guava

// FabricMC Maven - https://maven.fabricmc.net/
project.extra["fabricMixinVersion"] = "0.16.5+mixin.0.8.7" // net.fabricmc:sponge-mixin

// QuiltMC Maven - https://maven.quiltmc.org/repository/release/
project.extra["quiltLoaderVersion"] = "0.29.2-beta.5" // org.quiltmc:quilt-loader, org.quiltmc:quilt-loader-dependencies

val osArch: String = System.getProperty("os.arch")
val osName: String = System.getProperty("os.name")
if (osName.lowercase().contains("linux")) {
    project.extra["lwjglNativesName"] = "natives-linux" + (when (osArch) {
        "amd64" -> ""
        "arm" -> "-arm32"
        "aarch64" -> "-arm64"
        "ppc" -> "-ppc64le"
        "riscv" -> "-riscv64"
        else -> throw RuntimeException("Unsupported CPU Architecture for Linux!")
    })
} else if (osName.lowercase().contains("bsd")) {
    project.extra["lwjglNativesName"] = "natives-freebsd" + (when (osArch) {
        "amd64" -> ""
        else -> throw RuntimeException("Unsupported CPU Architecture for FreeBSD!")
    })
} else if (osName.lowercase().contains("mac")) {
    project.extra["lwjglNativesName"] = "natives-macos" + (when (osArch) {
        "amd64" -> ""
        "aarch64" -> "-arm64"
        else -> throw RuntimeException("Unsupported CPU Architecture for macOS!")
    })
} else if (osName.lowercase().contains("windows")) {
    project.extra["lwjglNativesName"] = "natives-macos" + (when (osArch) {
        "amd64" -> ""
        "x86" -> "-x86"
        "aarch64" -> "-arm64"
        else -> throw RuntimeException("Unsupported CPU Architecture for Windows!")
    })
} else {
    throw RuntimeException("Unsupported Operating System!")
}