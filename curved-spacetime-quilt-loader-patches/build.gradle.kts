plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    compileOnly(libs.quilt.loader)
    compileOnly(libs.quilt.loader.dependencies)
}
