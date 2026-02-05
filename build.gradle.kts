plugins {
    kotlin("jvm") version("2.3.0")
    id("java")
    id("maven-publish")
}

group = "net.mcbrawls"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.noxcrew.com/public/")
}

dependencies {
    api("net.minestom:minestom:2026.01.08-1.21.11")
    api("net.kyori:adventure-api:4.25.0")
    api("com.noxcrew.noxesium:api:3.0.0")
    api("org.apache.commons:commons-lang3:3.20.0")
    api("it.unimi.dsi:fastutil:8.5.12")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    jvmToolchain(25)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
        }
    }

    repositories {
        val mavenUrl = System.getenv("MAVEN_URL")
        if (mavenUrl != null) {
            maven {
                name = "envmaven"
                url = uri(mavenUrl)
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}
