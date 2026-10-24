import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.bammwise"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {

    implementation("org.apache.maven:maven-model-builder:3.9.9")

    intellijPlatform {
        intellijIdeaCommunity("2023.1.2")
        instrumentationTools()

//        pluginVerifier()
//        zipSigner()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}