plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "eu.mvojacek.paper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(17)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    relocate("kotlin", "eu.mvojacek.paper.containerpassthrough.shadow.kotlin")
    relocate("org.jetbrains.annotations", "eu.mvojacek.paper.containerpassthrough.shadow.org.jetbrains.annotations")
    relocate("org.intellij.lang.annotations", "eu.mvojacek.paper.containerpassthrough.shadow.org.intellij.lang.annotations")
    minimize()
}
