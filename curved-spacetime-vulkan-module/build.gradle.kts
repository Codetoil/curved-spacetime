plugins {
    id("java")
    id("java-library")
    id("io.github.sgtsilvio.gradle.javadoc-links")
    id("maven-publish")
    id("com.gradleup.shadow")
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

val lwjglVersion = project.findProperty("lwjglVersion") as String
val junitVersion = project.findProperty("junitVersion") as String

val nonJar = configurations.create("nonJar")

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    nonJar(files("../LICENSE.md", "../Notices.md"))

    api(project(":curved-spacetime-main-module"))

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))

    api("org.lwjgl:lwjgl-vulkan:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl-vulkan:$lwjglVersion:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-vulkan:$lwjglVersion:natives-macos-arm64")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

sourceSets {
    create("extra") {
        java.srcDir("src/extra/java")
    }
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
    dependencies {
        exclude(dependency("io.codetoil:curved-spacetime*"))
        include(dependency("org.lwjgl:.*"))
    }
    destinationDirectory = File("$rootDir/archive-quilt/modules")
    from(nonJar, file("src/main/java/module-info.java"))
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