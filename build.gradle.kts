plugins {
    java
    id("org.jetbrains.qodana") version "2025.2.1"
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

val nonJar by configurations.creating

dependencies {
    nonJar(files("LICENSE.md", "Notices.md"))
}

tasks.register("cleanJar") {
    run {
        file("$rootDir/installer-closed-jar/").deleteRecursively()
        file("$rootDir/installer-quilt/").deleteRecursively()
        files(
            "$rootDir/run-quilt/",
            "$rootDir/run-quilt/webserver-openapi",
            "$rootDir/run-quilt/modules",
            "$rootDir/run-closed-jar/",
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

tasks.register<Copy>("preRunQuilt") {
    from("$rootDir/installer-quilt/")
    into("$rootDir/run-quilt/")
    include("**")
    mustRunAfter(rootProject.subprojects.map { it.tasks.build })
}

tasks.register<Copy>("preRunClosedJar") {
    from("$rootDir/installer-closed-jar/")
    into("$rootDir/run-closed-jar/")
    include("**")
    mustRunAfter(rootProject.subprojects.map { it.tasks.build })
}

tasks.register<Copy>("nonJarCopyClosedJar") {
    from(nonJar)
    into("$rootDir/installer-closed-jar/")
    finalizedBy(tasks["preRunClosedJar"])
    mustRunAfter(rootProject.subprojects.map { it.tasks.build })
}

tasks.register<Copy>("nonJarCopyQuilt") {
    from(nonJar)
    into("$rootDir/installer-quilt/")
    finalizedBy(tasks["preRunQuilt"])
    mustRunAfter(rootProject.subprojects.map { it.tasks.build })
}

tasks.build {
    dependsOn(tasks["cleanJar"], rootProject.tasks.clean)
    finalizedBy(tasks["nonJarCopyClosedJar"], tasks["nonJarCopyQuilt"])
}