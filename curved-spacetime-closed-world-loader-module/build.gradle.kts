plugins {
    id("java")
    id("io.github.sgtsilvio.gradle.javadoc-links")
    id("maven-publish")
    id("org.graalvm.buildtools.native") version "0.11.3"
    id("com.gradleup.shadow") version "9.2.2"
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

base {
    archivesName = "curved-spacetime"
}

val nonJar by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    nonJar(files("../LICENSE.md", "../Notices.md"))

    implementation(project(":curved-spacetime-main-module"))
    implementation(project(":curved-spacetime-vulkan-module"))
    implementation(project(":curved-spacetime-glfw-module"))
    implementation(project(":curved-spacetime-render-module"))
    implementation(project(":curved-spacetime-glfw-render-module"))
    implementation(project(":curved-spacetime-vulkan-glfw-module"))
    implementation(project(":curved-spacetime-vulkan-render-module"))
    implementation(project(":curved-spacetime-vulkan-glfw-render-module"))
    implementation(project(":curved-spacetime-loader-module"))
    implementation(project(":curved-spacetime-webserver-module"))
    implementation(project(":curved-spacetime-webserver-openapi-module"))

    implementation("org.tinylog:tinylog-impl:${rootProject.extra["tinyLoggerVersion"]}")

    implementation("com.google.code.gson:gson:${rootProject.extra["gsonVersion"]}")
    implementation("com.google.guava:guava:${rootProject.extra["guavaVersion"]}")

    runtimeOnly("org.lwjgl:lwjgl:${rootProject.extra["lwjglVersion"]}")
    runtimeOnly("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}")
    runtimeOnly("org.lwjgl:lwjgl-vulkan:${rootProject.extra["lwjglVersion"]}")

    (rootProject.extra["lwjglNativesNames"] as List<*>)
        .forEach { runtimeOnly("org.lwjgl:lwjgl:${rootProject.extra["lwjglVersion"]}:${it}") }
    (rootProject.extra["lwjglNativesNames"] as List<*>)
        .forEach { runtimeOnly("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}:${it}") }
    runtimeOnly("org.lwjgl:lwjgl-vulkan:${rootProject.extra["lwjglVersion"]}:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-vulkan:${rootProject.extra["lwjglVersion"]}:natives-macos-arm64")

}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("curved-spacetime-0.1.0-SNAPSHOT-${rootProject.extra["osNameAndArch"]}")
            mainClass.set("io.codetoil.curved_spacetime.loader.closed_world.Main")
            debug.set(true)
            verbose.set(true)
            richOutput.set(true)
            quickBuild.set(false)
            configurationFileDirectories.from(file("src/graalvm"))
            resources {
                autodetect()
            }

            buildArgs.add("-H:+UnlockExperimentalVMOptions")
            buildArgs.add("-H:+PrintClassInitialization")
            buildArgs.add("-H:+GenerateEmbeddedResourcesFile")

            runtimeArgs.add("-Dfile.encoding=UTF-8")
            runtimeArgs.add("-Dsun.stdout.encoding=UTF-8")
            runtimeArgs.add("-Dsun.stderr.encoding=UTF-8")

            useFatJar.set(true)

            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            })

            System.setProperty("java.io.tmpdir", "$projectDir/build/tmp")
        }
    }
}

tasks.nativeCompile {
    dependsOn(tasks["cleanClosedNative"], rootProject.tasks.clean)
    finalizedBy((tasks["nativeFilesCopyClosedNative"] as Copy).from(outputDirectory))
}

tasks.register("cleanClosedNative") {
    run {
        val folder = file("$rootDir/archive-closed-world-native-${rootProject.extra["osNameAndArch"]}")
        if (folder.listFiles() != null && folder.listFiles()?.size != 0) {
            folder.listFiles()?.forEach { fileIt ->
                run {
                    if (fileIt.name.contains("curved-spacetime")) {
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

tasks.register<Copy>("nativeFilesCopyClosedNative") {
    into("$rootDir/archive-closed-world-native-${rootProject.extra["osNameAndArch"]}")
    exclude("sources", "reports", "embedded-resource.json")
    finalizedBy(tasks["nonJarCopyClosedNative"])
    mustRunAfter(tasks.nativeCompile)
}

tasks.register<Copy>("nonJarCopyClosedNative") {
    from(nonJar)
    into("$rootDir/archive-closed-world-native-${rootProject.extra["osNameAndArch"]}/")
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
    destinationDirectory = File("$rootDir/archive-closed-world-jar/")
    manifest {
        attributes(mapOf("Main-Class" to "io.codetoil.curved_spacetime.loader.closed_world.Main"))
    }
    from(nonJar)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Codetoil/curved-spacetime")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            pom {
                packaging = "jar"

                name = "Curved Spacetime Closed World Loader Module"
                url = "https://github.com/Codetoil/curved-spacetime"
                inceptionYear = "2025"
                licenses {
                    license {
                        name = "GPL-3.0-or-later"
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "codetoil"
                        name = "Anthony Michalek (Codetoil)"
                        email = "ianthisawesomee@gmail.com"
                        url = "https://codetoil.io"
                        roles = setOf("owner", "architect", "developer")
                    }
                }
                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/Codetoil/curved-spacetime/issues"
                }
                ciManagement {
                    system = "GitHub Actions"
                    url = "https://github.com/Codetoil/curved-spacetime/actions"
                }
                scm {
                    connection = "scm:git:git://github.com/Codetoil/curved-spacetime.git"
                    developerConnection = "scm:git:ssh://github.com/Codetoil/curved-spacetime.git"
                    url = "https://github.com/Codetoil/curved-spacetime"
                }
            }
            from(components["shadow"])
        }
    }
}

tasks.javadocLinks {
    urlProvider = { id -> urlProviderFunc(id) }
}