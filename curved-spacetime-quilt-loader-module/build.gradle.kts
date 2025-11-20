plugins {
    id("java")
    id("io.github.sgtsilvio.gradle.javadoc-links")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

val nonJar by configurations.creating

dependencies {
    nonJar(files("../LICENSE.md", "../Notices.md"))
    implementation(project(":curved-spacetime-loader-module"))
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

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
    exclude(
        "fabric-installer.launchwrapper.json", "quilt_installer.json",
        "LICENSE", "LICENSE.txt", "META-INF/LICENSE", "LICENSE_MixinExtras", "LICENSE_quilt-loader",
        "changelog/**"
    )
    destinationDirectory = File("$rootDir/archive-quilt")
    manifest {
        attributes(mapOf("Main-Class" to "io.codetoil.curved_spacetime.loader.quiltmc.KnotCurvedSpacetime"))
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

                name = "Curved Spacetime Quilt Loader Module"
                url = "https://github.com/Codetoil/curved-spacetime"
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

tasks.javadocLinks {
    urlProvider = { id -> urlProviderFunc(id) }
}