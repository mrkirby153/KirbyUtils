plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.22")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}