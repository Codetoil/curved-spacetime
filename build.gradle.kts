plugins {
    id("io.freefair.aggregate-javadoc") version "9.5.0"
    id("org.jetbrains.qodana") version "2026.2.0"
    id("io.github.sgtsilvio.gradle.javadoc-links") version "0.10.0" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
    id("org.graalvm.buildtools.native") version "1.1.9" apply false
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

// The distribution layout — which module's finished artifact goes where under archive-quilt/ and
// archive-closed-world-jar/.
//
// These directories are populated by copying finished jars into them. No jar task points its
// destinationDirectory here, and none should. Redirecting a jar task makes the distribution
// directory that task's *output* directory, with two consequences that cost a day to track down:
//
//   1. Gradle owns it. Stale-output cleanup and cleanJar both empty task output directories, and
//      any Gradle invocation can trigger that — including the background daemon the VS Code Gradle
//      extension runs on its own schedule, not just an explicit ./gradlew build.
//   2. The IDE points at it. The root project's classpath resolves each subproject to its jar's
//      destinationDirectory, so the language server adopts archive-quilt/** as library paths, then
//      reports "Project 'curved-spacetime' is missing required library" the moment (1) happens.
//
// Keeping jar output at the Gradle default (build/libs) gives the IDE a stable path that nothing
// empties, and makes the distribution a downstream artifact rather than a build output.
//
// Columns: module name, subdirectory within the archive, name of the task producing the artifact.
val quiltDistribution = listOf(
    Triple("curved-spacetime-main-module", "", "jar"),
    Triple("curved-spacetime-quilt-loader-module", "", "shadowJar"),
    Triple("curved-spacetime-cli-module", "cli-modules", "jar"),
    Triple("curved-spacetime-render-module", "modules", "jar"),
    Triple("curved-spacetime-simulator-module", "modules", "jar"),
    Triple("curved-spacetime-vulkan-render-module", "modules", "jar"),
    Triple("curved-spacetime-vulkan-glfw-render-module", "modules", "jar"),
    Triple("curved-spacetime-glfw-render-module", "modules", "shadowJar"),
    Triple("curved-spacetime-vulkan-module", "modules", "shadowJar"),
    Triple("curved-spacetime-webserver-module", "webserver-modules", "jar"),
    Triple("curved-spacetime-webserver-openapi-module", "webserver-modules", "jar"),
)

val closedWorldDistribution = listOf(
    Triple("curved-spacetime-closed-world-loader-module", "", "shadowJar"),
)

fun Copy.distributionOf(entries: List<Triple<String, String, String>>) {
    entries.forEach { (moduleName, subdirectory, artifactTask) ->
        into(subdirectory) {
            from(project(":$moduleName").tasks.named(artifactTask))
        }
    }
}

tasks.register<Copy>("jarCopyQuilt") {
    description = "Copy the module jars into the output directory for the Quilt Jar"
    into("$rootDir/archive-quilt/")
    distributionOf(quiltDistribution)
    mustRunAfter("cleanJar")
}

tasks.register<Copy>("jarCopyClosedJar") {
    description = "Copy the shadow jar into the output directory for the Closed World Jar"
    into("$rootDir/archive-closed-world-jar/")
    distributionOf(closedWorldDistribution)
    mustRunAfter("cleanJar")
}

tasks.register("cleanJar") {
    description = "Clean the output directories for the Quilt and Closed World Versions of Stale Files"
    run {
        files(
            "$rootDir/archive-quilt/",
            "$rootDir/archive-quilt/webserver-modules",
            "$rootDir/archive-quilt/cli-modules",
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
    finalizedBy(
        tasks["nonJarCopyClosedJar"], tasks["nonJarCopyQuilt"],
        tasks["jarCopyClosedJar"], tasks["jarCopyQuilt"],
    )
}