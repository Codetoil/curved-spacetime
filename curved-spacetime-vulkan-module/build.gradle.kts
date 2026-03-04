plugins {
    id("java")
    id("java-library")
    id("io.github.sgtsilvio.gradle.javadoc-links")
    id("maven-publish")
    id("com.gradleup.shadow")
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

val junitVersion: String by project
val sgffmBindingsVersion: String by project

val nonJar by configurations.creating

dependencies {
    nonJar(files("../LICENSE.md", "../Notices.md"))

    api(project(":curved-spacetime-main-module"))
    api("io.codetoil:simple-graphics-ffm-bindings-vulkan:$sgffmBindingsVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}


tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
    dependencies {
        exclude(dependency("io.codetoil:.*"))
    }
    destinationDirectory = File("$rootDir/archive-quilt/modules")
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

                name = "Curved Spacetime Vulkan Module"
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
                        name = "Anthony Michalek / Angelina Michalek (Codetoil)"
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