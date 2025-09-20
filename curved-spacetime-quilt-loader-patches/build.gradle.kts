plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

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
    compileOnly("org.quiltmc:quilt-loader:${rootProject.extra["quiltLoaderVersion"]}")
    compileOnly("org.quiltmc:quilt-loader-dependencies:${rootProject.extra["quiltLoaderVersion"]}")
    compileOnly("com.google.code.gson:gson:${rootProject.extra["gsonVersion"]}")
    compileOnly("com.google.guava:guava:${rootProject.extra["guavaVersion"]}")
    compileOnly("net.fabricmc:sponge-mixin:${rootProject.extra["fabricMixinVersion"]}")
}
