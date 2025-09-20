import java.nio.file.Paths

plugins {
    id ("java")
    id ("maven-publish")
}

java {
    withSourcesJar()
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

/*configurations {
    dependencyScope("vulkanGLFWRenderModuleRuntimeOnly") {
        canBeConsumed = false
        canBeResolved = false
    }
    dependencyScope("webserverOpenAPIModuleRuntimeOnly") {
        canBeConsumed = false
        canBeResolved = false
    }
    dependencyScope("vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly") {
        canBeConsumed = false
        canBeResolved = false
    }
}*/


repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        url = uri("https://maven.quiltmc.org/repository/release/")
    }
}

val getLWJGLNativesName = rootProject.extra["getLWJGLNativesName"]
val lwjglNativesName: String? = (getLWJGLNativesName as? () -> String)?.invoke()

dependencies {
    implementation (project(":curved-spacetime-main-module"))
    implementation ("org.quiltmc:quilt-loader:${rootProject.extra["quiltLoaderVersion"]}") {
        exclude("annotations")
    }
    implementation ("org.quiltmc:quilt-loader-dependencies:${rootProject.extra["quiltLoaderVersion"]}")

    testImplementation (platform("org.junit:junit-bom:${rootProject.extra["junitVersion"]}"))
    testImplementation ("org.junit.jupiter:junit-jupiter")
    testImplementation ("org.junit.platform:junit-platform-launcher")

    implementation ("org.tinylog:tinylog-impl:${rootProject.extra["tinyLoggerVersion"]}")

    runtimeOnly (project(":curved-spacetime-quilt-tweaker-agent"))
    implementation ("com.google.code.gson:gson:${rootProject.extra["gsonVersion"]}")
    implementation ("com.google.guava:guava:${rootProject.extra["guavaVersion"]}")
    implementation ("net.fabricmc:sponge-mixin:${rootProject.extra["fabricMixinVersion"]}")

    runtimeOnly ("org.lwjgl:lwjgl:${rootProject.extra["lwjglVersion"]}")
    runtimeOnly ("org.lwjgl:lwjgl:${rootProject.extra["lwjglVersion"]}:$lwjglNativesName")
    runtimeOnly ("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}")
    runtimeOnly ("org.lwjgl:lwjgl-glfw:${rootProject.extra["lwjglVersion"]}:$lwjglNativesName")
    runtimeOnly ("org.lwjgl:lwjgl-vulkan:${rootProject.extra["lwjglVersion"]}")
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        runtimeOnly ("org.lwjgl:lwjgl-vulkan:${rootProject.extra["lwjglVersion"]}:$lwjglNativesName")
    }

    /*vulkanGLFWRenderModuleRuntimeOnly project(":curved-spacetime-glfw-module")
    vulkanGLFWRenderModuleRuntimeOnly project(":curved-spacetime-render-module")
    vulkanGLFWRenderModuleRuntimeOnly project(":curved-spacetime-vulkan-module")
    vulkanGLFWRenderModuleRuntimeOnly project(":curved-spacetime-vulkan-glfw-module")
    vulkanGLFWRenderModuleRuntimeOnly project(":curved-spacetime-vulkan-render-module")
    vulkanGLFWRenderModuleRuntimeOnly project(":curved-spacetime-glfw-render-module")
    vulkanGLFWRenderModuleRuntimeOnly project(":curved-spacetime-vulkan-glfw-render-module")

    webserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-webserver-module")
    webserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-webserver-openapi-module")

    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-glfw-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-render-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-vulkan-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-vulkan-glfw-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-vulkan-render-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-glfw-render-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-vulkan-glfw-render-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-webserver-module")
    vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly project(":curved-spacetime-webserver-openapi-module")*/
}

/*configurations {
    resolvable("runtimeClasspathWithVulkanGLFWRenderModule") {
        canBeConsumed = false
        canBeDeclared = false
        extendsFrom(implementation, vulkanGLFWRenderModuleRuntimeOnly)
    }
    resolvable("runtimeClasspathWithWebserverOpenAPIModule") {
        canBeConsumed = false
        canBeDeclared = false
        extendsFrom(implementation, webserverOpenAPIModuleRuntimeOnly)
    }
    resolvable("runtimeClasspathWithVulkanGLFWRenderModuleAndWebserverOpenAPIModule") {
        canBeConsumed = false
        canBeDeclared = false
        extendsFrom(implementation, vulkanGLFWRenderModuleAndWebserverOpenAPIModuleRuntimeOnly)
    }
}*/

val initQuiltTweakerAgent: Set<Task> = project(":curved-spacetime-quilt-tweaker-agent")
    .getTasksByName("jar", false)

fun safeishWorkingDirectory(workingPath: java.nio.file.Path): File
{
    return if(workingPath.toFile().exists())
        workingPath.toFile()
    else
        if (workingPath.toFile().mkdirs())
            workingPath.toFile()
        else throw RuntimeException("Couldn't create $workingPath")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn(initQuiltTweakerAgent)
    jvmArgs("-javaagent:../curved-spacetime-quilt-tweaker-agent/build/libs/curved-spacetime-quilt-tweaker-agent-0.1.0-SNAPSHOT.jar")
    workingDir = safeishWorkingDirectory(Paths.get(rootDir.toString(), "test"))
}

tasks.register("runTrivial", JavaExec::class) {
    classpath = java.sourceSets["main"].runtimeClasspath

    mainClass = "io.codetoil.curved_spacetime.loader.KnotCurvedSpacetime"
    jvmArgs = mutableListOf(
        "-Dloader.gameJarPath=../curved-spacetime-main-module/build/libs/curved-spacetime-main-module-0.1.0-SNAPSHOT.jar",
        "-Dloader.development=true",
        "-javaagent:../curved-spacetime-quilt-tweaker-agent/build/libs/curved-spacetime-quilt-tweaker-agent-0.1.0-SNAPSHOT.jar",
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8"
    )
    workingDir = safeishWorkingDirectory(Paths.get(rootDir.toString(), "runTrivial"))
    dependsOn(initQuiltTweakerAgent)
}

/*tasks.register('runTrivialWithVulkanGLFWRenderModule', JavaExec) {
    classpath = configurations.runtimeClasspathWithVulkanGLFWRenderModule + sourceSets.main.runtimeClasspath

    mainClass = 'io.codetoil.curved_spacetime.loader.KnotCurvedSpacetime'
    jvmArgs = ['-Dloader.gameJarPath=../curved-spacetime-main-module/build/libs/curved-spacetime-main-module-0.1.0-SNAPSHOT.jar',
               '-Dloader.development=true',
               '-javaagent:../curved-spacetime-quilt-tweaker-agent/build/libs/curved-spacetime-quilt-tweaker-agent-0.1.0-SNAPSHOT.jar',
               '-Dfile.encoding=UTF-8',
               '-Dsun.stdout.encoding=UTF-8',
               '-Dsun.stderr.encoding=UTF-8']

    setWorkingDir(Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModule").toFile().exists() ? Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModule").toFile() : Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModule").toFile().mkdirs() ? Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModule").toFile() : null)
    dependsOn(initQuiltTweakerAgent)
}

tasks.register('runTrivialWithWebserverOpenAPI', JavaExec) {
    classpath = configurations.runtimeClasspathWithWebserverOpenAPIModule + sourceSets.main.runtimeClasspath

    mainClass = 'io.codetoil.curved_spacetime.loader.KnotCurvedSpacetime'
    jvmArgs = ['-Dloader.gameJarPath=../curved-spacetime-main-module/build/libs/curved-spacetime-main-module-0.1.0-SNAPSHOT.jar',
               '-Dloader.development=true',
               '-javaagent:../curved-spacetime-quilt-tweaker-agent/build/libs/curved-spacetime-quilt-tweaker-agent-0.1.0-SNAPSHOT.jar',
               '-Dfile.encoding=UTF-8',
               '-Dsun.stdout.encoding=UTF-8',
               '-Dsun.stderr.encoding=UTF-8']

    setWorkingDir(Paths.get(getWorkingDir().parent, "runTrivialWithWebserverOpenAPIModule").toFile().exists() ? Paths.get(getWorkingDir().parent, "runTrivialWithWebserverOpenAPIModule").toFile() : Paths.get(getWorkingDir().parent, "runTrivialWithWebserverOpenAPIModule").toFile().mkdirs() ? Paths.get(getWorkingDir().parent, "runTrivialWithWebserverOpenAPIModule").toFile() : null)
    dependsOn(initQuiltTweakerAgent)
}

tasks.register('runTrivialWithVulkanGLFWRenderModuleAndWebserverOpenAPI', JavaExec) {
    classpath = configurations.runtimeClasspathWithVulkanGLFWRenderModuleAndWebserverOpenAPIModule + sourceSets.main.runtimeClasspath

    mainClass = 'io.codetoil.curved_spacetime.loader.KnotCurvedSpacetime'
    jvmArgs = ['-Dloader.gameJarPath=../curved-spacetime-main-module/build/libs/curved-spacetime-main-module-0.1.0-SNAPSHOT.jar',
               '-Dloader.development=true',
               '-javaagent:../curved-spacetime-quilt-tweaker-agent/build/libs/curved-spacetime-quilt-tweaker-agent-0.1.0-SNAPSHOT.jar',
               '-Dfile.encoding=UTF-8',
               '-Dsun.stdout.encoding=UTF-8',
               '-Dsun.stderr.encoding=UTF-8']

    setWorkingDir(Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModuleAndWebserverOpenAPIModule").toFile().exists() ? Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModuleAndWebserverOpenAPIModule").toFile() : Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModuleAndWebserverOpenAPIModule").toFile().mkdirs() ? Paths.get(getWorkingDir().parent, "runTrivialWithVulkanGLFWRenderModuleAndWebserverOpenAPIModule").toFile() : null)
    dependsOn(initQuiltTweakerAgent)
}*/


tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "io.github.codetoil.curved_spacetime.loader.KnotCurvedSpacetime"))
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
            components.forEach { softwareComponent -> from(softwareComponent) }
        }
    }
}