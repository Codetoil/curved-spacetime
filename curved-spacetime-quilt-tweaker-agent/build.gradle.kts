plugins {
    java
    id("maven-publish")
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(8);
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.fabricmc.net/");
    }
    maven {
        url = uri("https://maven.quiltmc.org/repository/release/");
    }
}

val bundle by configurations.creating

dependencies {
    bundle(project(":curved-spacetime-quilt-loader-patches"))
    testImplementation(platform("org.junit:junit-bom:${rootProject.extra["junitVersion"]}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(mapOf("Premain-Class" to "QuiltTweakerAgent", "Can-Retransform-Classes" to true))
    }
    from(bundle) {
        into("")
        rename { "curved-spacetime-quilt-loader-patches.jar" }
    }
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

                name = "Curved Spacetime Quilt Tweaker Agent"
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
                        name = "Anthony Michalek / Angelina Michalek (Codetoil)"
                        email = "ianthisawesomee@gmail.com"
                        url = "https://codetoil.io"
                        roles = setOf("owner", "arcitect", "developer")
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
            components.forEach({ softwareComponent -> from(softwareComponent) })
        }
    }
}