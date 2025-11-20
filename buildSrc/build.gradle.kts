plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.21"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("io.freefair.aggregate-javadoc:io.freefair.aggregate-javadoc.gradle.plugin:9.1.0")
}