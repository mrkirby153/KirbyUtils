import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("kirbyutils.common")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://repo.dmulloy2.net/content/groups/public/")
    maven(url = "https://repo.aikar.co/nexus/content/groups/aikar/")
}

dependencies {
    implementation(project(":KirbyUtils-Common"))
    implementation("co.aikar:acf-core:0.5.0-SNAPSHOT")
    implementation("net.kyori:adventure-text-serializer-legacy:4.14.0")
    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.4.0-SNAPSHOT")
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    dependencies {
        exclude(dependency("co.aikar:acf-core"))
    }
}

tasks {
    val shadowJar by getting(ShadowJar::class)
    named("build") {
        dependsOn(shadowJar)
    }
}