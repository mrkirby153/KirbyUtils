import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://repo.dmulloy2.net/content/groups/public/")
    maven(url = "https://repo.aikar.co/nexus/content/groups/aikar/")
}

dependencies {
    implementation(project(":KirbyUtils-Bukkit"))
    implementation(project(":KirbyUtils-Common"))
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    relocate("co.aikar", "me.mrkirby153.kcutils.testplugin.thirdparty")
}

tasks {
    val jar by getting(Jar::class)
    runServer {
        minecraftVersion("1.21.8")
        dependsOn(jar)
    }
    named("compileKotlin") {
        dependsOn(":KirbyUtils-Bukkit:shadowJar")
    }
}