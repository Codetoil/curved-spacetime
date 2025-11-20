plugins {
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation ("org.apache.maven:maven-model:3.9.11")
    implementation ("org.apache.maven:maven-artifact:3.9.11")

    api ("io.freefair.okhttp:io.freefair.okhttp.gradle.plugin:9.1.0")
}