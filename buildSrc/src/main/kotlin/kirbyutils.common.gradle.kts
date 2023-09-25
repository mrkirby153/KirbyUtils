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

fun publishUrl() = if (project.version.toString().endsWith("-SNAPSHOT")) {
    "https://repo.mrkirby153.com/repository/maven-snapshots/"
} else {
    "https://repo.mrkirby153.com/repository/maven-releases"
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
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