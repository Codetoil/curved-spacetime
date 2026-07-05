plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

val lwjglVersion = project.findProperty("lwjglVersion") as String
val junitVersion = project.findProperty("junitVersion") as String
val fabricMixinVersion = project.findProperty("fabricMixinVersion") as String
val quiltLoaderVersion = project.findProperty("quiltLoaderVersion") as String

dependencies {
    compileOnly("org.quiltmc:quilt-loader:${quiltLoaderVersion}")
    compileOnly("org.quiltmc:quilt-loader-dependencies:${quiltLoaderVersion}")
}
