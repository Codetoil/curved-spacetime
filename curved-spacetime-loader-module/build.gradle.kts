plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.2.2"
    id("org.graalvm.buildtools.native") version "0.11.1"
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

val nonJar by configurations.creating

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        url = uri("https://maven.quiltmc.org/repository/release/")
    }
}

dependencies {
    nonJar(files("../LICENSE.md"))
    implementation(project(":curved-spacetime-quilt-loader-patches"))
    implementation("org.quiltmc:quilt-loader:${rootProject.extra["quiltLoaderVersion"]}") {
        exclude("annotations")
    }
    implementation("org.quiltmc:quilt-loader-dependencies:${rootProject.extra["quiltLoaderVersion"]}")

    testImplementation(platform("org.junit:junit-bom:${rootProject.extra["junitVersion"]}"))

    implementation("org.tinylog:tinylog-impl:${rootProject.extra["tinyLoggerVersion"]}")

    implementation("com.google.code.gson:gson:${rootProject.extra["gsonVersion"]}")
    implementation("com.google.guava:guava:${rootProject.extra["guavaVersion"]}")
    implementation("net.fabricmc:sponge-mixin:${rootProject.extra["fabricMixinVersion"]}")

}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("curved-spacetime-debug")
            mainClass.set("io.codetoil.curved_spacetime.loader.KnotCurvedSpacetime")
            debug.set(true)
            verbose.set(true)
            richOutput.set(true)
            quickBuild.set(false)

            configurationFileDirectories.from(file("src/graalvm"))

            buildArgs.add(
                "--initialize-at-build-time=" +
                        "org.quiltmc.loader.impl.filesystem.QuiltUnifiedFileSystemProvider," +
                        "org.quiltmc.loader.impl.filesystem.QuiltMemoryFileSystemProvider," +
                        "org.quiltmc.loader.impl.filesystem.QuiltJoinedFileSystemProvider," +
                        "org.quiltmc.loader.impl.filesystem.QuiltZipFileSystemProvider," +
                        "org.quiltmc.loader.impl.filesystem.QuiltFSP"
            )
            jvmArgs.add("-Dloader.gameJarPath=../../../../installer/curved-spacetime-main-module-0.1.0-SNAPSHOT-all.jar")
            jvmArgs.add("-Dloader.development=true")
            jvmArgs.add("-Dfile.encoding=UTF-8")
            jvmArgs.add("-Dsun.stdout.encoding=UTF-8")
            jvmArgs.add("-Dsun.stderr.encoding=UTF-8")
            jvmArgs.add("-Dloader.validation.level=5")
            jvmArgs.add("-Dloader.log.level=TRACE")
            jvmArgs.add("-Dorg.lwjgl.util.Debug=true")
            jvmArgs.add("-Dorg.lwjgl.util.DebugLoader=true")

            runtimeArgs.add("")

            useFatJar.set(true)

            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            })
        }
    }
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
    exclude(
        "fabric-installer.launchwrapper.json", "quilt_installer.json",
        "LICENSE", "LICENSE.txt", "META-INF/LICENSE", "LICENSE_MixinExtras", "LICENSE_quilt-loader",
        "changelog/**"
    )
    dependencies {
        exclude(dependency("io.codetoil:.*"))
    }
    destinationDirectory = File("$rootDir/installer")
    manifest {
        attributes(mapOf("Main-Class" to "io.codetoil.curved_spacetime.loader.KnotCurvedSpacetime"))
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

                name = "Curved Spacetime Loader Module"
                url = "https://codetoil.io/curved-spacetime"
                inceptionYear = "2023"
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