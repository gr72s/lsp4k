plugins {
    kotlin("jvm") version "1.9.21"
    id("java")
    `maven-publish`
}

group = "cc.green"
version = "1.0-SNAPSHOT"

extra["name"] = "LSP for Kotlin"
extra["description"] = "JsonRPC implement"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty.websocket:jetty-websocket-jetty-client:12.0.5")
    implementation("org.eclipse.jetty.websocket:jetty-websocket-jetty-api:12.0.5")
    implementation("org.eclipse.jetty.websocket:jetty-websocket-jetty-server:12.0.5")

    implementation(kotlin("reflect"))
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")
}

tasks.test {
    useJUnitPlatform()
}