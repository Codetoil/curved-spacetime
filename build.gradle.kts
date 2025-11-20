plugins {
    //id("java")
    id("org.jetbrains.qodana") version "2025.2.2"
}

// Maven Central - https://repo1.maven.org/maven2/
project.extra["lwjglVersion"] = "3.3.6" // org.lwjgl:lwjgl, org.lwjgl:lwjgl-glfw, org.lwjgl:lwjgl-vulkan
project.extra["junitVersion"] = "6.0.1" // org.junit:junit-bom
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

val osArch: String = System.getProperty("os.arch")
val osName: String = System.getProperty("os.name")
if (osName.lowercase().contains("linux")) {
    project.extra["osNameAndArch"] = "linux" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        "arm" -> "-arm32"
        "aarch64" -> "-arm64"
        "ppc" -> "-ppc64le"
        "riscv" -> "-riscv64"
        else -> throw RuntimeException("Unsupported CPU Architecture for Linux!")
    })
} else if (osName.lowercase().contains("bsd")) {
    project.extra["osNameAndArch"] = "freebsd" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        else -> throw RuntimeException("Unsupported CPU Architecture for FreeBSD!")
    })
} else if (osName.lowercase().contains("mac")) {
    project.extra["osNameAndArch"] = "macos" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        "aarch64" -> "-arm64"
        else -> throw RuntimeException("Unsupported CPU Architecture for macOS!")
    })
} else if (osName.lowercase().contains("windows")) {
    project.extra["osNameAndArch"] = "windows" + (when (osArch) {
        "amd64", "x86_64" -> "-x64"
        "x86" -> "-x86"
        "aarch64" -> "-arm64"
        else -> throw RuntimeException("Unsupported CPU Architecture for Windows!")
    })
} else {
    throw RuntimeException("Unsupported Operating System!")
}

val nonJar by configurations.creating

dependencies {
    nonJar(files("LICENSE.md", "Notices.md"))

    rootProject.subprojects.filter { project -> !project.name.contains("quilt-loader-patches") }
        .forEach { subproject ->
            subproject.plugins.withId("java") {
                javadoc(subproject)
            }
        }
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            url = uri("https://maven.quiltmc.org/repository/release/")
        }
    }
}

tasks.register("cleanJar") {
    run {
        files(
            "$rootDir/archive-quilt/",
            "$rootDir/archive-quilt/webserver-openapi",
            "$rootDir/archive-quilt/modules",
            "$rootDir/archive-closed-world-jar/",
        ).forEach { folderIt ->
            if (folderIt.listFiles() != null && folderIt.listFiles().size != 0) {
                folderIt.listFiles().forEach { fileIt ->
                    run {
                        if (fileIt.name.contains(".jar")) {
                            fileIt.delete();
                            println("deleted: ${fileIt.path}")
                        }
                        if (fileIt.name.contains("LICENSE.md")) {
                            fileIt.delete()
                            println("deleted: ${fileIt.path}")
                        }
                        if (fileIt.name.contains("Notices.md")) {
                            fileIt.delete()
                            println("deleted: ${fileIt.path}")
                        }
                    }
                }
            }
        }
    }
}

tasks.register<Copy>("nonJarCopyClosedJar") {
    from(nonJar)
    into("$rootDir/archive-closed-world-jar/")
    mustRunAfter(rootProject.subprojects.map { it.tasks.build })
}

tasks.register<Copy>("nonJarCopyQuilt") {
    from(nonJar)
    into("$rootDir/archive-quilt/")
    mustRunAfter(rootProject.subprojects.map { it.tasks.build })
}

tasks.build {
    dependsOn(tasks["cleanJar"], rootProject.tasks.clean)
    finalizedBy(tasks["nonJarCopyClosedJar"], tasks["nonJarCopyQuilt"])
}