plugins {
    id("java-library")
    id("io.github.sgtsilvio.gradle.javadoc-links")
    id("maven-publish")
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

val nonJar by configurations.creating

dependencies {
    nonJar(files("../LICENSE.md", "../Notices.md"))
    api("org.tinylog:tinylog-api:${rootProject.extra["tinyLoggerVersion"]}")

    testImplementation(platform("org.junit:junit-bom:${rootProject.extra["junitVersion"]}"))
}

tasks.test {
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

                name = "Curved Spacetime Loader Module"
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
            from(components["java"])
        }
    }
}

tasks.javadocLinks {
    urlProvider = { id -> urlProviderFunc(id) }
}