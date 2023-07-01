plugins {
    id("kirbyutils.common")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    compileOnly("org.springframework:spring-tx:6.0.4")
    compileOnly("org.springframework:spring-context:6.0.0")
}