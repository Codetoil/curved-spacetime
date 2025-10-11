plugins {
    id ("java")
    id ("java-library")
    id ("maven-publish")
}

java {
    withSourcesJar()
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

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
    api (project(":curved-spacetime-main-module"))

    implementation ("org.tinylog:tinylog-impl:${rootProject.extra["tinyLoggerVersion"]}")

    api ("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}")
    runtimeOnly ("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}:${rootProject.extra["lwjglNativesName"]}")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
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

                    name = "Curved Spacetime GLFW Module"
                    url = "https://codetoil.io/curved-spacetime"
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
                components.forEach { softwareComponent -> from(softwareComponent) }
            }
        }
    }