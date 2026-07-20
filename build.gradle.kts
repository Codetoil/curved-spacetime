plugins {
    id("io.freefair.aggregate-javadoc") version "9.5.0"
    id("org.jetbrains.qodana") version "2026.1.3"
    id("io.github.sgtsilvio.gradle.javadoc-links") version "0.10.0" apply false
    id("com.gradleup.shadow") version "9.5.1" apply false
    id("org.graalvm.buildtools.native") version "1.1.5" apply false
}

val nonJar = configurations.create("nonJar")

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
    description = "Clean the output directories for the Quilt and Closed World Versions of Stale Files"
    run {
        files(
            "$rootDir/archive-quilt/",
            "$rootDir/archive-quilt/webserver-modules",
            "$rootDir/archive-quilt/cli-modules",
            "$rootDir/archive-quilt/simulator-modules",
            "$rootDir/archive-quilt/modules",
            "$rootDir/archive-closed-world-jar/",
        ).forEach { folderIt ->
            if (folderIt.listFiles() != null && folderIt.listFiles()!!.size != 0) {
                folderIt.listFiles()!!.forEach { fileIt ->
                    run {
                        if (fileIt.name.contains(".jar")) {
                            fileIt.delete()
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
    description = "Put the nonJar stuff into the output directory for the Closed World Jar"
    from(nonJar)
    into("$rootDir/archive-closed-world-jar/")
    mustRunAfter(rootProject.subprojects.filter { it2 -> it2.tasks.any { it.name == "build" } }
        .map { it.tasks.build })
}

tasks.register<Copy>("nonJarCopyQuilt") {
    description = "Put the nonJar stuff into the output directory for the Quilt Jar"
    from(nonJar)
    into("$rootDir/archive-quilt/")
    mustRunAfter(rootProject.subprojects.filter { it2 -> it2.tasks.any { it.name == "build" } }
        .map { it.tasks.build })
}

tasks.build {
    dependsOn(tasks["cleanJar"], rootProject.tasks.clean)
    finalizedBy(tasks["nonJarCopyClosedJar"], tasks["nonJarCopyQuilt"])
}