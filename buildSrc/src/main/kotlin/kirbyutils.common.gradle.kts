plugins {
    id("java")
    id("org.jetbrains.dokka")
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
}

group = "me.mrkirby153"
version = "5.0-SNAPSHOT"

fun publishUrl() = if (project.version.toString().endsWith("-SNAPSHOT")) {
    "https://repo.mrkriby153.com/repository/maven-snapshots/"
} else {
    "https://repo.mrkriby153.com/repository/maven-releases"
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "mrkirby153"
            url = uri(publishUrl())
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
}