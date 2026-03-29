plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

val junitVersion: String by project
val fabricMixinVersion: String by project
val quiltLoaderVersion: String by project

dependencies {
    compileOnly("org.quiltmc:quilt-loader:$quiltLoaderVersion")
    compileOnly("org.quiltmc:quilt-loader-dependencies:$quiltLoaderVersion")
}
