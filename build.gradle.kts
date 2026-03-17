plugins {
    kotlin("jvm")
    id("java")
    id("maven-publish")
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    group = "net.mcbrawls.slate"
    version = "2.0.1"

    repositories {
        mavenCentral()
        maven("https://maven.enginehub.org/repo/")
        maven("https://maven.noxcrew.com/public/")
    }

    kotlin {
        jvmToolchain(25)
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }
}

subprojects {
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
}
