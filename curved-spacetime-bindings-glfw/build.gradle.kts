plugins {
    id("java-library")
    id("io.github.sgtsilvio.gradle.javadoc-links")
    id("maven-publish")
}

group = "io.codetoil"
version = "0.1.0-SNAPSHOT"

val junitVersion: String by project
val fabricMixinVersion: String by project
val quiltLoaderVersion: String by project

val nonJar by configurations.creating

dependencies {
    nonJar(files("../LICENSE.md", "../Notices.md"))
}

tasks.javadocLinks {
    urlProvider = { id -> urlProviderFunc(id) }
}