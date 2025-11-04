plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.2.2"
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
    nonJar(files("../LICENSE.md", "../Notices.md"))
    api(project(":curved-spacetime-main-module"))

    testImplementation(platform("org.junit:junit-bom:${rootProject.extra["junitVersion"]}"))

    implementation("org.tinylog:tinylog-impl:${rootProject.extra["tinyLoggerVersion"]}")

    api("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}")
    (rootProject.extra["lwjglNativesNames"] as List<*>)
        .forEach { runtimeOnly("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}:${it}") }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
    dependencies {
        exclude(dependency("io.codetoil:.*"))
        include(dependency("org.lwjgl:.*"))
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

                name = "Curved Spacetime GLFW Module"
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