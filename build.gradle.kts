plugins {
    id("io.freefair.aggregate-javadoc") version "9.8.2"
    id("org.jetbrains.qodana") version "2026.2.2"
    id("io.github.sgtsilvio.gradle.javadoc-links") version "0.10.0" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
}

val nonJar = configurations.create("nonJar")

dependencies {
    nonJar(files("LICENSE.md", "Notices.md"))

    // quilt-loader-patches holds patched copies of upstream FabricMC/QuiltMC classes, and as such is
    // excluded from the main javadoc batch
    rootProject.subprojects.filter { project -> !project.name.contains("quilt-loader-patches") }
        .forEach { subproject ->
            subproject.plugins.withId("java") {
                javadoc(subproject)
            }
        }
}

subprojects {
    // quilt-loader-patches holds patched copies of upstream FabricMC/QuiltMC classes, so it is
    // excluded here for the same reason it is excluded from the aggregate javadoc above.
    if (!name.contains("quilt-loader-patches")) {
        // Fail the build on any javadoc warning, so documentation coverage cannot regress.
        tasks.withType<Javadoc>().configureEach {
            (options as StandardJavadocDocletOptions).addBooleanOption("Xwerror", true)
        }

        // LWJGL publishes non-modular javadoc, so linking to it from our named modules emits
        // "packages ... are in the unnamed module" warnings that no amount of documentation can
        // fix — and -Xwerror would promote them to errors. The javadoc-links plugin offers no way
        // to exclude a dependency, so drop those -linkoffline entries from the options file it
        // generates. LWJGL types are referenced with {@code}, not {@link}, so nothing is lost.
        plugins.withId("io.github.sgtsilvio.gradle.javadoc-links") {
            tasks.named("javadocLinks") {
                doLast {
                    val optionsFile = temporaryDir.resolve("javadoc.options")
                    if (optionsFile.exists()) {
                        optionsFile.writeText(
                            optionsFile.readLines()
                                .filterNot { it.contains("org.lwjgl") }
                                .joinToString("\n")
                        )
                    }
                }
            }
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

// The distribution layout — which module's finished artifact goes where under build/archive/ and run/
//
// These directories are populated by copying finished jars into them. No jar task points its
// destinationDirectory here, and none should. Redirecting a jar task makes the distribution
// directory that task's *output* directory, with two consequences that cost a day to track down:
//
//   1. Gradle owns it. Stale-output cleanup and cleanJar both empty task output directories, and
//      any Gradle invocation can trigger that — including the background daemon the VS Code Gradle
//      extension runs on its own schedule, not just an explicit ./gradlew build.
//   2. The IDE points at it. The root project's classpath resolves each subproject to its jar's
//      destinationDirectory, so the language server adopts build/archive/** as library paths, then
//      reports "Project 'curved-spacetime' is missing required library" the moment (1) happens.
//
// Keeping jar output at the Gradle default (build/libs) gives the IDE a stable path that nothing
// empties, and makes the distribution a downstream artifact rather than a build output.
//
// Columns: module name, subdirectory within the archive, name of the task producing the artifact.
val distribution = listOf(
    Triple("curved-spacetime-main-module", "", "jar"),
    Triple("curved-spacetime-quilt-loader-module", "", "shadowJar"),
    Triple("curved-spacetime-cli-module", "cli-modules", "jar"),
    Triple("curved-spacetime-render-module", "modules", "jar"),
    Triple("curved-spacetime-simulator-module", "modules", "jar"),
    Triple("curved-spacetime-render-vulkan-module", "modules", "jar"),
    Triple("curved-spacetime-render-vulkan-glfw-module", "modules", "jar"),
    Triple("curved-spacetime-render-glfw-module", "modules", "shadowJar"),
    Triple("curved-spacetime-vulkan-module", "modules", "shadowJar"),
    Triple("curved-spacetime-webserver-module", "webserver-modules", "jar"),
)

fun Copy.distributionOf(entries: List<Triple<String, String, String>>) {
    entries.forEach { (moduleName, subdirectory, artifactTask) ->
        into(subdirectory) {
            from(project(":$moduleName").tasks.named(artifactTask))
        }
    }
}

tasks.register("cleanRun") {
    description = "Clean the run directory"
    run {
        files(
            "$rootDir/run/",
            "$rootDir/run/webserver-modules",
            "$rootDir/run/cli-modules",
            "$rootDir/run/modules",
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

tasks.register<Copy>("moduleJarCopyArchive") {
    description = "Copy the module jars into the Archive Output Directory"
    into("$rootDir/build/archive/")
    distributionOf(distribution)
    mustRunAfter("clean")
}

tasks.register<Copy>("moduleJarCopyRun") {
    description = "Copy the module jars into the run directory"
    into("$rootDir/run/")
    distributionOf(distribution)
    mustRunAfter("cleanRun")
}

tasks.register<Copy>("nonJarCopyArchive") {
    description = "Copy the nonJar files into the Archive Output directory"
    from(nonJar)
    into("$rootDir/build/archive/")
    // Ordered against assemble, not build: mustRunAfter is a soft constraint that simply does not
    // apply when the named task is absent from the graph, so pointing it at build would silently
    // drop the ordering whenever the distribution is assembled without the test suite.
    mustRunAfter(rootProject.subprojects.filter { it2 -> it2.tasks.any { it.name == "assemble" } }
        .map { it.tasks.assemble }, "clean")
}

tasks.register<Copy>("nonJarCopyRun") {
    description = "Copy the nonJar files into the run directory"
    from(nonJar)
    into("$rootDir/run/")
    // See nonJarCopyArchive for why this is ordered against assemble rather than build.
    mustRunAfter(rootProject.subprojects.filter { it2 -> it2.tasks.any { it.name == "assemble" } }
        .map { it.tasks.assemble }, "cleanRun")
}

// Hung off assemble rather than build: assembling the distribution is artifact production, not
// verification. `build` is `assemble` + `check`, so this still fires on a plain ./gradlew build,
// but it also lets a release path skip the test suite — ./gradlew assemble —
// without silently producing empty archive directories.
tasks.assemble {
    dependsOn(tasks["cleanRun"], rootProject.tasks.clean)
    finalizedBy(
        tasks["nonJarCopyArchive"], tasks["moduleJarCopyArchive"],
        tasks["nonJarCopyRun"], tasks["moduleJarCopyRun"]
    )
}