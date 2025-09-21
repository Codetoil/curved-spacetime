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
    api (project(":curved-spacetime-render-module"))
    api (project(":curved-spacetime-glfw-module"))
    api (project(":curved-spacetime-vulkan-module"))
    api (project(":curved-spacetime-glfw-render-module"))
    api (project(":curved-spacetime-vulkan-render-module"))
    api (project(":curved-spacetime-vulkan-glfw-module"))

    testImplementation (platform("org.junit:junit-bom:${rootProject.extra["junitVersion"]}"))
    testImplementation ("org.junit.jupiter:junit-jupiter")

    implementation ("org.tinylog:tinylog-impl:${rootProject.extra["tinyLoggerVersion"]}")

    implementation ("org.quiltmc:quilt-loader-dependencies:${rootProject.extra["quiltLoaderVersion"]}")
    implementation ("com.google.code.gson:gson:${rootProject.extra["gsonVersion"]}")
    implementation ("com.google.guava:guava:${rootProject.extra["guavaVersion"]}")
    implementation ("net.fabricmc:sponge-mixin:${rootProject.extra["fabricMixinVersion"]}")
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

                name = "Curved Spacetime Vulkan GLFW RenderModule"
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
            components.forEach { softwareComponent -> from(softwareComponent) }
        }
    }
}