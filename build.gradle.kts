plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    id("org.glavo.compile-module-info-plugin") version "2.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
}

group = "org.glavo"
version = "0.4.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.tukaani:xz:1.10")
}

tasks.withType<JavaCompile> {
    options.release.set(8)
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}
